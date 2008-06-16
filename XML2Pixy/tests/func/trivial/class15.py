class A:
    pass

class B:
    def __init__(self, a):
        pass

class C(B):
    pass

class D(A, C):
    pass
