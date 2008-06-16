import m2.m

def foo():
    print 'm1.m.foo'

def bar():
    m2.m.foo()
        