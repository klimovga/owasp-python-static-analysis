import sys
import os
import compiler
from visitor import TreeVisitor
import analysis.source

def usage():
    print '''Usage:
    parser.py file [dest-file]
    parser.py directory dest-dir'''
    sys.exit()

# Check cmd arguments
#   arg 1 - must be dir or file
#   arg 2 - if arg 1 is dir, must be present and do not exist or be dir
def checkArgs(argv):
    if not (os.path.isdir(argv[1]) or os.path.isfile(argv[1])):
        print argv[1] + ' is not file or directory'
        sys.exit()
    if os.path.isdir(argv[1]) and len(argv) > 2:
        if os.path.exists(argv[2]) and not os.path.isdir(argv[2]):
            print 'Destination must be directory'
            sys.exit()
    if os.path.isfile(argv[1]) and len(argv) > 2:
        if os.path.isdir(argv[2]):
            print 'Destination must be file'
            sys.exit()

# Visitor for dir tree
# Translates all .py files and serializes AST into .xml in a specified directory (cmd arg 2)
def visitDir(arg, dirname, names):
    src = arg['src']
    dest = arg['dest']
    relative = dirname[len(src):]
    for file in names:
        srcFile = os.path.abspath(src + '/' + relative + '/' + file)
        destFile = os.path.abspath(dest + '/' + relative + '/' + file + '.xml')
        
        if os.path.isfile(srcFile) and srcFile.endswith('.py'):
            dirname = os.path.dirname(destFile)
            if not os.path.exists(dirname):
                os.makedirs(os.path.dirname(destFile))
            parseFile(srcFile, destFile, arg['base'])

# Parses src file and serializes AST into dest file in xml format
def parseFile(src, dest, base):
    print 'Parsing "' + src + '" into "' + dest + '"'
#    ast = analysis.source.process_file(src, base)
    ast = compiler.parseFile(src)
    xmlFile = open(dest, 'w')
    visitor = TreeVisitor(src)
    xmlFile.write(compiler.walk(ast, visitor, visitor).xml)
    xmlFile.flush()
    xmlFile.close()

def main(argv):  
    # Check argv for arguments
    if len(argv) == 1:
        usage()
        
    checkArgs(argv)

    if os.path.isfile(argv[1]):
        dest = argv[1] + '.xml'
        if len(argv) > 2: dest = argv[2]
        pythonPath = os.path.split(argv[1])
        
        parseFile(argv[1], dest, os.path.realpath(pythonPath[0]))
        
    elif os.path.isdir(argv[1]):
        if not os.path.exists(argv[2]):
            os.makedirs(argv[2])
        
        cfg = {'src': argv[1], 'dest': argv[2], 'base': os.path.realpath(argv[1])}
        os.path.walk(argv[1], visitDir, cfg)
    else:
        pass
        

if __name__ == '__main__':
    main(sys.argv)
