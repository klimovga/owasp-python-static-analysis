import sys

class Evil:
    def __init__(self, x):
        self.evil = x

    def xss(self):
        print self.evil

    def xss1(self, x):
        print x

a = Evil(sys.argv[1])
a.xss()
a.xss1(sys.argv[1])
