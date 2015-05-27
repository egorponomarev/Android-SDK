#!/bin/bash

GRADLE=../gradlew
TOP=.
BUILD=$TOP/build/outputs/eclipse
OUT=$BUILD/FeedFM-Android-SDK

ZIP=FeedFM-Android-SDK-eclipse.zip

rm -fr $BUILD
mkdir -p $OUT

# copy source over
mkdir -p $OUT/src
cp -r $TOP/src/main/java/* $OUT/src

# resources
mkdir -p $OUT/res
cp -r $TOP/src/main/res/* $OUT/res

# dependencies
$GRADLE assemble
mkdir -p $OUT/libs
cp $TOP/build/outputs/libs/* $OUT/libs

# eclipse can't handle these
rm -f $OUT/libs/*.aar
# don't include support libs
rm -f $OUT/libs/support-*.jar

# assets
mkdir -p $OUT/assets

# AndroidManifest.xml
cp $TOP/src/main/AndroidManifest.xml $OUT

# tell Eclipse about target and identify that we are a lib
#echo 'target=android-16' > $OUT/project.properties
echo 'android.library=true' >> $OUT/project.properties

# zip things up nicely
cd $BUILD
zip $ZIP -r *
cd -

