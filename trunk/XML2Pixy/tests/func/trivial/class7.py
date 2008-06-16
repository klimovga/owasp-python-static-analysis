class A:
    def c(self):
        return None

a = A()
a.b = A()
x = a.b.c()
