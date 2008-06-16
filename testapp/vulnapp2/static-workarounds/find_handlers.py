#!/usr/bin/python
#
# Find all handlers in .py files and create one module that calls any of them

import os
import string
import sys


def split_to_root(root_dir, dir):
    dirs = []
    parent = dir
    while parent != root_dir:
        (parent, child) = os.path.split(parent)
        dirs.append(child)

    dirs.reverse()

    return dirs


def has_handler(dir_name, module_name):
    if dir_name not in sys.path:
        print "Added to sys.path " + dir_name
        sys.path[:0] = [dir_name]

    print "Importing module: " + module_name
    module = __import__(module_name)
    sys.path = sys.path[1:]
    print "Imported module: " + module_name

    if "handler" in dir(module):
        return True
    else:
        return False


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print "Use: %s project_directory [excluded_modules]" % sys.argv[0]
        sys.exit(1)

    proj_dir = sys.argv[1]
    if len(sys.argv) > 2:
        excluded = sys.argv[2].split(",")
    else:
        excluded = []

    # add to module path
    sys.path[:0] = [ proj_dir ]
    
    handlers = []

    for root, dirs, files in os.walk(proj_dir):
        prefix = os.path.commonprefix([proj_dir, root])
        for f in files:
            if f.rfind(".py") == len(f) - len(".py"):
                module_name = f[:-len(".py")]
                if f != "__init__" and module_name not in excluded:
                    if has_handler(root, module_name):
                        handlers.append((split_to_root(proj_dir, root), module_name))

    out = open("entry.py", "w+")

    for (pkgs, name) in handlers:
        print >>out, 'import ' + string.join(pkgs + [name], ".")

    print >>out, ""
    print >>out, "# non-deterministically call modules"
    print >>out, ""
    print >>out, "random = raw_input()"
    print >>out, "req = _GET"
    print >>out, "args = []"

    first = True

    for (pkgs, name) in handlers:
        if first:
            print >>out, "if random == '%s':" % name
            first = False
        else:
            print >>out, "elif random == '%s':" % name

        print >>out, "    %s.handler(req, args)" % name

    out.close()
