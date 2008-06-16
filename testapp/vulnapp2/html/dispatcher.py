import mod_python.apache
import mod_python.util 

import os
import string
import imp
import re
import base64
import urllib
import new

from types import *

# now compile a regular expression out of it:
exp = "\\" + string.join([".html"], "$|\\")
suff_matcher = re.compile(exp)

#module map
apache = mod_python.apache
util = mod_python.util

def handler(req):

    # use direct access to _req for speed
    _req = req._req

    args = {}

    # process input, if any
    fs = util.FieldStorage(req, keep_blank_values=1)
    req.form = fs

    # step through fields
    for field in fs.list:

       # if it is a file, make File()
       if field.filename:
           val = File(field)
       else:
           val = field.value

       if args.has_key(field.name):
           args[field.name].append(val)
       else:
           args[field.name] = [val]

    # at the end, we replace lists with single values
    for arg in args.keys():
        if len(args[arg]) == 1:
            args[arg] = args[arg][0]

    # import the script
    path, module_name =  os.path.split(_req.filename)

    
    # get rid of the suffix
    module_name = suff_matcher.sub("", module_name)

    # import module (or reload if needed)
    module = apache.import_module(module_name, _req, [path])

    # does it have an __auth__?
    realm, user, passwd = process_auth(req, module)

    # resolve the object ('traverse')
    try:
        object = resolve_object(req, module, "handler", realm, user, passwd)
    except AttributeError:
        raise apache.SERVER_RETURN, apache.HTTP_NOT_FOUND

    # not callable, a class or an aunbound method
    if not callable(object) or \
       str(type(object)) == "<type 'class'>" \
       or (hasattr(object, 'im_self') and not object.im_self):

        result = str(object)
        
    else:
        # callable, (but not a class or unbound method)

        # we need to weed out unexpected keyword arguments
        # and for that we need to get a list of them. There
        # are a few options for callable objects here:

        if str(type(object)) == "<type 'instance'>":
            # instances are callable when they have __call__()
            object = object.__call__

        if hasattr(object, "func_code"):
            # function
            fc = object.func_code
            expected = fc.co_varnames[0:fc.co_argcount]
        elif hasattr(object, 'im_func'):
            # method
            fc = object.im_func.func_code
            expected = fc.co_varnames[1:fc.co_argcount]

    result = object(req, args)
    #result = str(args)

    if result:
        if type(result) is dict:
		location = result["Location"]
		del result["Location"]
		qs = urllib.urlencode(result)
		req.headers_out["Location"] = location + "?" + qs
		req.status = apache.HTTP_SEE_OTHER
		req.send_http_header()
		return apache.HTTP_SEE_OTHER

	result = str(result)

        # unless content_type was manually set, we will attempt
        # to guess it
        if not req._content_type_set:
            # make an attempt to guess content-type
            if string.lower(string.strip(result[:100])[:6]) == '<html>' \
               or string.find(result,'</') > 0:
                req.content_type = 'text/html'
            else:
                req.content_type = 'text/plain'

        req.send_http_header()
        req.write(result)
        return apache.OK
    else:
        return apache.HTTP_INTERNAL_SERVER_ERROR

def process_auth(req, object, realm="unknown", user=None, passwd=None):

    found_auth, found_access = 0, 0

    # because ap_get_basic insists on making sure that AuthName and
    # AuthType directives are specified and refuses to do anything
    # otherwise (which is technically speaking a good thing), we
    # have to do base64 decoding ourselves.
    #
    # to avoid needless header parsing, user and password are parsed
    # once and the are received as arguments
    if not user and req.headers_in.has_key("Authorization"):
        try:
            s = req.headers_in["Authorization"][6:]
            s = base64.decodestring(s)
            user, passwd = string.split(s, ":", 1)
        except:
            raise apache.SERVER_RETURN, apache.HTTP_BAD_REQUEST

    if hasattr(object, "__auth_realm__"):
        realm = object.__auth_realm__

    if type(object) == type(process_auth):
        # functions are a bit tricky

        if hasattr(object, "func_code"):
            func_code = object.func_code

            if "__auth__" in func_code.co_names:
                i = list(func_code.co_names).index("__auth__")
                __auth__ = func_code.co_consts[i+1]
                if hasattr(__auth__, "co_name"):
                    __auth__ = new.function(__auth__, globals())
                found_auth = 1

            if "__access__" in func_code.co_names:
                # first check the constant names
                i = list(func_code.co_names).index("__access__")
                __access__ = func_code.co_consts[i+1]
                if hasattr(__access__, "co_name"):
                    __access__ = new.function(__access__, globals())
                found_access = 1

            if "__auth_realm__" in func_code.co_names:
                i = list(func_code.co_names).index("__auth_realm__")
                realm = func_code.co_consts[i+1]

    else:
        if hasattr(object, "__auth__"):
            __auth__ = object.__auth__
            found_auth = 1
        if hasattr(object, "__access__"):
            __access__ = object.__access__
            found_access = 1

    if found_auth:

        if not user:
            s = 'Basic realm = "%s"' % realm
            req.err_headers_out["WWW-Authenticate"] = s
            raise apache.SERVER_RETURN, apache.HTTP_UNAUTHORIZED    

        if callable(__auth__):
            rc = __auth__(req, user, passwd)
        else:
            if type(__auth__) == type({}): # dictionary
                rc = __auth__.has_key(user) and __auth__[user] == passwd
            else:
                rc = __auth__
            
        if not rc:
            s = 'Basic realm = "%s"' % realm
            req.err_headers_out["WWW-Authenticate"] = s
            raise apache.SERVER_RETURN, apache.HTTP_UNAUTHORIZED    

    if found_access:

        if callable(__access__):
            rc = __access__(req, user)
        else:
            if type(__access__) in (type([]), type(())):
                rc = user in __access__
            else:
                rc = __access__

        if not rc:
            raise apache.SERVER_RETURN, apache.HTTP_FORBIDDEN

    return realm, user, passwd

def resolve_object(req, obj, object_str, realm=None, user=None, passwd=None):
    """
    This function traverses the objects separated by .
    (period) to find the last one we're looking for.
    """

    parts = object_str.split('.')

    for n in range(len(parts)):

        obj = getattr(obj, parts[n])
        obj_type = type(obj)

        # object cannot be a module or a class
        if obj_type in [ClassType, ModuleType]:
            raise apache.SERVER_RETURN, apache.HTTP_NOT_FOUND

        if n < (len(parts)-1):
            
            # all but the last object ...

            # ...must be instance
            if obj_type != InstanceType:
                raise apache.SERVER_RETURN, apache.HTTP_NOT_FOUND
            
        realm, user, passwd = process_auth(req, obj, realm,
                                           user, passwd)

    return obj

    
class File:
    """ Like a file, but also has headers and filename
    """

    def __init__(self, field):

        # steal all the file-like methods
        for m in dir(field.file):
            self.__dict__[m] = getattr(field.file, m)

        self.headers = field.headers
        self.filename = field.filename
    
