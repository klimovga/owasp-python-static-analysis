#!/bin/sh

XML2PIXY_HOME=`realpath $0`
XML2PIXY_HOME=`dirname $XML2PIXY_HOME`

LIBDIR=$XML2PIXY_HOME/lib
BUILDDIR=$XML2PIXY_HOME/build

java -Dpixy.home=$XML2PIXY_HOME -jar $BUILDDIR/xml2pixy.jar $*

