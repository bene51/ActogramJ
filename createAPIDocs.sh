#!/bin/sh

JAR="/home/bene/fiji/java/linux/jdk1.6.0_21/bin/jar"
CP="-classpath .:$JARF/ij.jar:$JARF/itext-1.3.jar:$JARF/pal-optimization.jar"

sh createJar.sh
mkdir tmp
mkdir docs
mv ActogramJ_.jar tmp/
cd tmp;

jar -xf ActogramJ_.jar

javadoc $CP -d ../docs/ -subpackages actoj


