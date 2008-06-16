#/bin/bash

print_usage() 
{
   echo "$0 - Python web applications static analysis tool"
   echo "Usage: $0 [options] webappdir entrypoint "
   echo "    webappdir - dir with asessed web application"
   echo "    entrypoint - filename where PythonHandler function is defined"
   echo "Options: "
   echo "  --help - print this usage"
}

set -e

STATIC_HOME=`dirname $0`/..
STATIC_HOME=`cd $STATIC_HOME; pwd`
ANALYSIS_HOME=`pwd`/analysis
PYPARSER_HOME=$STATIC_HOME/lib/pyparser

if [ ! -d "$PYPARSER_HOME" ]; then
    PYPARSER_HOME=$STATIC_HOME/pyparser/src
    
    if [ ! -d "$PYPARSER_HOME" ]; then
        echo "pyparser home not found"
        exit 1
    fi
fi

if [ "$1" == "--help" ]; then
	print_usage
	exit 1
fi

if [ -z "$1" ] || [ "!" -d "$1" ]; then
    print_usage
    exit 1
fi

if [ -z "$2" ]; then
    print_usage
    exit 1
fi

SRC_DIR=`cd $1; pwd`

mkdir -p $ANALYSIS_HOME
echo -----------------------------------------
echo "Parsing Python sources "
echo -----------------------------------------
echo $PYPARSER_HOME/pyparser.sh $SRC_DIR $ANALYSIS_HOME
$PYPARSER_HOME/pyparser.sh $SRC_DIR $ANALYSIS_HOME

echo -----------------------------------------
echo "Performing vulnerability search"
echo -----------------------------------------
cd $ANALYSIS_HOME
java -Xmx512m -Xms512m  -Dpixy.home=$STATIC_HOME -jar $STATIC_HOME/lib/xml2pixy.jar $2.xml


