class A:
    def foo(self):
        print "A.foo()"

class B(A):
    def bar(self):
        print "B.bar()"

class C(B):
    def foo(self):
        print "C.foo()"
