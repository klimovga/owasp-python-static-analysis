class A:
    def foo(self):
        print "A.foo()"

class B:
    def bar(self):
        print "B.bar()"

class C(A, B):
    def foo(self):
        print "C.foo()"

    def bar(self):
        print "C.bar()"
