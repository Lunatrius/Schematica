#!/bin/bash

# current working directory (root)
dir=$PWD

# move to the language directory
cd resources/lunatrius/schematica/assets/lang

# find all *.xml files are remove their extension
files=`find *.xml | sed s/\.xml//g`

# for each file
for file in $files
do
	# print the name
	echo $file
done

# save a list of all currently available languages
echo "$files" > lang.txt

# go back to the original working directory
cd $dir
