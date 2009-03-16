#!/bin/sh

PARSER_HOME=`dirname $0`
PARSER_HOME=`cd $PARSER_HOME; pwd`

# corrupts standard path!
# export PYTHONPATH=$PARSER_HOME

python $PARSER_HOME/pyparser/pyparser.py $1 $2

