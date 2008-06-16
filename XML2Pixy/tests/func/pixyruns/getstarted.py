# a VERY simple demo file for getting started;
# see doc/readme.txt for usage instructions;
# for more complex demos, take a look into the "testfiles" folder

a = 'hi'
b = _GET['evil']

print a    # this one is OK
print b    # XSS vulnerability
