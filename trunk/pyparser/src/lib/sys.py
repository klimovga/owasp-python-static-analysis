#!/usr/bin/env python

def _get_argc():
    """
    NAME: IMPL.sys._get_argc
    NATIVE
    """
    return int()

def _get_arg(i):
    """
    NAME: IMPL.sys._get_arg
    NATIVE
    """
    if not isinstance(i, int):
        TypeConstraintError
    return string()

def _get_argv():
    i = 0
    argv = []
    while i < _get_argc():
        argv.append(_get_arg(i))
        i += 1
    return argv

argv = _get_argv()

maxint = int()

def setrecursionlimit(n):
    pass

# vim: tabstop=4 expandtab shiftwidth=4
