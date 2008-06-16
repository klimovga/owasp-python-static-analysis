#!/usr/bin/python

import sys
import pyparser.pyparser

if __name__ == "__main__":
    print ""
    print "Using Python path: " + str(sys.path)
    print ""
    pyparser.pyparser.main(sys.argv)
