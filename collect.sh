#!/bin/sh
rm -rf collect
mkdir collect

# only collect from subprojects with a version number in them
# excludes :vanilla, excludes :floader-only
cp ./*1.*/build/libs/*.jar collect

#mv ./collect/*sources*.jar collect/sources
