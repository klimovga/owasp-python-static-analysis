def inline_me(str, args):
  res = str

  for a in args:
    res = res + a

  return res


inline_me("foo", [1, 2])
