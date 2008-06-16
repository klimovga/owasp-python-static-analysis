#!/bin/bash

set -e

STATIC_HOME=`dirname $0`
STATIC_HOME=`cd $STATIC_HOME; pwd`
echo STATIC_HOME=$STATIC_HOME

if [ -z "$STATIC_HOME" ]; then
    echo STATIC_HOME is undefined. Aborting.
    exit 1
fi

echo -n Retrieving build number from SVN...
REVISION=`svn info | grep Revision:`
R_NUMBER=${REVISION#Revision:}
R_NUMBER=${R_NUMBER## }
echo $REVISION 

DIST_NAME=static.$R_NUMBER

RELEASE_DIR=$STATIC_HOME/Release
DIST_DIR=$RELEASE_DIR/$DIST_NAME

if [ -d "$DIST_DIR" ]; then
	echo Performing cleanup from previos interrupted build
	rm -rf $DIST_DIR
fi

mkdir -p $DIST_DIR

LIB_DIR=$DIST_DIR/lib
mkdir $LIB_DIR

cd $STATIC_HOME

echo -e "\nBuilding up Pixy\n****************\n"
cd $STATIC_HOME/Pixy
ant build-jar

cp build/*.jar $LIB_DIR
# We need pixy.jar to build XML2Pixy
cp build/*.jar $STATIC_HOME/XML2Pixy/lib/

echo -e "\nBuilding up XML2Pixy\n********************\n"

cd $STATIC_HOME/XML2Pixy
ant build-jar

cp build/*.jar $LIB_DIR
cp $STATIC_HOME/XML2Pixy/lib/log4j*.jar $LIB_DIR
cd $STATIC_HOME

mkdir $DIST_DIR/config
find $STATIC_HOME/Pixy/config -maxdepth 1 -type f -exec cp '{}' $DIST_DIR/config ';'
find $STATIC_HOME/XML2Pixy/config -maxdepth 1 -type f -exec cp '{}' $DIST_DIR/config ';'
mkdir $DIST_DIR/bin
find $STATIC_HOME/bin -maxdepth 1 -type f -exec cp '{}' $DIST_DIR/bin ';'
echo "Сборка $R_NUMBER" >$DIST_DIR/README.koi8
cat $STATIC_HOME/doc/README.koi8 >>$DIST_DIR/README.koi8

cp -r $STATIC_HOME/pyparser/src $DIST_DIR/lib/pyparser

echo -e "\nRemoving svn control entries\n****************************\n"
cd $DIST_DIR
find $DIST_DIR -name '.svn' | xargs echo rm -rf >/dev/null 2>/dev/null

cd $RELEASE_DIR
echo Creating $BASENAME.tar.gz
tar zcf $DIST_NAME.tar.gz $DIST_NAME
cd $STATIC_HOME
#rm -rf Release

echo -e "\n\nDISTRIBUTIVE OF STATIC ANALYSIS TOOL IS BUILT SUCCESSFULLY\n\n"
