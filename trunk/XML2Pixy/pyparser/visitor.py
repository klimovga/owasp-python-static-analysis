from compiler.visitor import ASTVisitor
from compiler.ast import Node
import inspect
import string
import utils

# Custom AST Visitor
class TreeVisitor(ASTVisitor):
    
    def __init__(self, filename):
        ASTVisitor.__init__(self)
        self.xml = '<?xml version="1.0"?>\n\n'
        self.filename = filename
        self.indent_step = 2
        self.indent_val = 0
    
    # Default Node visitor
    # Creates xml tag for Node based on it's name, attributes, nested tags
    # Attributes are fields with integer or string values and 'lineno' field
    # Others are assumed to be nested tags
    # Walk through all nested Node objects
    #
    # TODO correct xml transformation
    # It is better to visit nodes with special methods for correct processing of nodes' fields 
    def default(self, node, *args):
        members = inspect.getmembers(node)
        nodeName = node.__class__.__name__

        children = []
        attributes = []
        for m in members:
            if m[0].startswith('_') and m[0] != '_contexts' or m[0] == 'filename': continue
            if m[1].__class__.__name__ == 'instancemethod': continue
            if m is None: continue
            
            if isinstance(m[1], int) or isinstance(m[1], str) or m[0] == 'lineno':
                attributes.append(m)
            else:
                children.append(m)

        filename = self.filename
        if hasattr(node, 'filename'):
            filename = node.filename

        self.indent()
        self.xml += '<' + nodeName + ' filename="' + utils.escapedRepr(filename) + '"' 
        for attr in attributes:
            self.xml += ' ' + attr[0] + '="' + utils.escapedRepr(attr[1]) + '"'
        self.xml += '>\n'
        self.indent_val += self.indent_step
        
        for child in children:
            self.indent()
            self.xml += '<' + child[0]+ '>\n'
            self.indent_val += self.indent_step
            if child[0] == '_contexts':
                try:
                    self.xml += utils.escapedRepr(child[1][None][0])
                except:
                    pass
            else :
                self.dispatchList(child[1], args)
            self.indent_val -= self.indent_step
            self.indent()
            self.xml += '</' + child[0]+ '>\n'
        
        self.indent_val -= self.indent_step
        self.indent()
        self.xml += '</' + nodeName + '>\n'


    # Used to process arrays, lists, tuples of Node objects
    # If argument is not Node object 'returns' escaped string representation
    def dispatchList(self, l, *args):
        if isinstance(l, Node):
            self.dispatch(l, args)
        elif type(l) is list or type(l) is tuple:
            for elem in l:
                self.dispatchList(elem, args)
        else:
            self.indent()
            self.xml += utils.escapedRepr(l) + "\n"
            
    def visitFunction(self, node, *args):
        self.indent()
        self.xml += '<Function flags="' + utils.escapedRepr(node.flags) + '" '
        filename = self.filename
        if hasattr(node, 'filename'):
            filename = node.filename

        self.xml += 'filename="' + utils.escapedRepr(filename) + '" '
        self.xml += 'lineno="' + utils.escapedRepr(node.lineno) + '" '
        self.xml += 'name="' + utils.escapedRepr(node.name) + '">\n'
        self.indent_val += self.indent_step
        
        self.indent()
        self.xml += '<argnames>\n'
        self.indent_val += self.indent_step
        self.indent()
        first = True
        for arg in node.argnames:
            if first: first = False
            else: self.xml += ','

            if type(arg) == tuple:
                raise "Found tuple argument: " + str(arg)
            self.xml += utils.escape(arg)
        self.xml += '\n'
        self.indent_val -= self.indent_step
        self.indent()
        self.xml += '</argnames>\n'
        
        self.indent()
        self.xml += '<code>\n'
        self.indent_val += self.indent_step
        self.dispatch(node.code, args)
        self.indent_val -= self.indent_step
        self.indent()
        self.xml += '</code>\n'
        
        self.indent()
        self.xml += '<decorators>\n'
        self.indent_val += self.indent_step
        self.dispatchList(node.decorators, args)
        self.indent_val -= self.indent_step
        self.indent()
        self.xml += '</decorators>\n'

        self.indent()
        self.xml += '<defaults>\n'
        self.indent_val += self.indent_step
        self.dispatchList(node.defaults, args)
        self.indent_val -= self.indent_step
        self.indent()
        self.xml += '</defaults>\n'

        self.indent()
        self.xml += '<doc>\n'
        self.indent_val += self.indent_step
        self.dispatchList(node.doc, args)
        self.indent_val -= self.indent_step
        self.indent()
        self.xml += '</doc>\n'
        
        self.indent()
        self.xml += '<kwargs>\n'
        self.indent_val += self.indent_step
        self.dispatchList(node.kwargs, args)
        self.indent_val -= self.indent_step
        self.indent()
        self.xml += '</kwargs>\n'

        self.indent()
        self.xml += '<varargs>\n'
        self.indent_val += self.indent_step
        self.dispatchList(node.varargs, args)
        self.indent_val -= self.indent_step
        self.indent()
        self.xml += '</varargs>\n'
        
        if hasattr(node, "_contexts"):
            self.indent()
            self.xml += '<_contexts>\n'
            self.indent_val += self.indent_step
            try:
                self.indent()
                self.xml += utils.escapedRepr(node._contexts[None][0]) + "\n"
	    except:
                pass
            self.indent_val -= self.indent_step
            self.indent()
            self.xml += '</_contexts>\n'

        self.indent_val -= self.indent_step
        self.indent()
        self.xml += '</Function>\n'
        
    def visitImport(self, node, *args):
        filename = self.filename
        if hasattr(node, 'filename'):
            filename = node.filename

        self.indent()
        self.xml += '<Import '
        self.xml += 'filename="' + utils.escapedRepr(filename) + '" '
        self.xml += 'lineno="' + utils.escapedRepr(node.lineno) + '">\n'
        self.indent_val += self.indent_step
        
        self.indent()
        self.xml += '<names>'
        
        comma = False
        for x in node.names:
            if comma:
                self.xml += ','
            else:
                comma = True
            self.xml += utils.escapedRepr(x[0])
            
        self.xml += '</names>\n'
        
        self.indent_val -= self.indent_step
        self.indent()
        self.xml += '</Import>\n'

    def visitFrom(self, node, *args):
        filename = self.filename
        if hasattr(node, 'filename'):
            filename = node.filename

        self.indent()
        self.xml += '<From '
        self.xml += 'filename="' + utils.escapedRepr(filename) + '" '
        self.xml += 'lineno="' + utils.escapedRepr(node.lineno) + '" '
        self.xml += 'modname="' + utils.escapedRepr(node.modname) + '">\n'
        self.indent_val += self.indent_step

        self.indent()
        self.xml += '<names>'
        
        comma = False
        for x in node.names:
            if comma:
                self.xml += ','
            else:
                comma = True
            self.xml += utils.escapedRepr(x[0])
            
        self.xml += '</names>\n'

        self.indent_val -= self.indent_step
        self.indent()
        self.xml += '</From>\n'

    def indent(self):
        self.xml += " " * self.indent_val
