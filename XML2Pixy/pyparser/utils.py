# Escapes xml spec-symbols & = &amp;, ' = &apos;, " = &quot;, < = &lt;, > = &gt;
def escape(s):
    s = s.replace('&', '&amp;')
    s = s.replace('\'', '&apos;')
    s = s.replace('"', '&quot;')
    s = s.replace('<', '&lt;')
    s = s.replace('>', '&gt;')
    return s

# Makes escaped string representation of an object using repr() built-in function
# Strings are represented w/o enclosing quotes.
def escapedRepr(obj):
    if obj is None: return ''    
    s = repr(obj)
    if type(obj) is str: s = s[1:-1]
    return escape(s)
