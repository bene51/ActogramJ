#!/bin/sh

JAVAC="/home/bene/fiji/java/linux/jdk1.6.0_21/bin/javac"
JAR="/home/bene/fiji/java/linux/jdk1.6.0_21/bin/jar"

PWD=`pwd`
FIJIF="$PWD/../../"
JARF="$FIJIF/jars/"
VIBF="$FIJIF/src-plugins/VIB-lib/"

JAVACOPTS="-source 1.5 -target 1.5 -classpath .:$JARF/ij.jar:$JARF/itext-1.3.jar:$JARF/pal-optimization.jar"
SRC="actoj www CHANGES plugins.config"

test ! -d tempdir || rm -rf tempdir
mkdir tempdir
tar cvf - $SRC | (cd tempdir; tar xvf -)

(cd tempdir && \
	rm -rf ActogramJ_.jar && \
	$JAVAC $JAVACOPTS actoj/ActogramJ_.java && \
	$JAR cvf ../ActogramJ_.jar `find . -type f`) && \
rm -rf tempdir

