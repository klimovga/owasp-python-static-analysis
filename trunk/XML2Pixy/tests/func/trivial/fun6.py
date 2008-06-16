def foo(x):
    if x == 0:
        return 1
    else:
        return foo(x - 1)
