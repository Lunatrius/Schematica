# current working directory (root)
dir=$PWD

# move to the language directory
cd resources/lunatrius/schematica/assets/lang

# find all *.utf8 files are remove their extension
files=`find *.utf8 | sed s/\.utf8//g`

# for each file
for file in $files
do
	# print the name
	echo $file

	# convert the *.utf8 file to a *.lang file
	native2ascii -encoding utf8 $file.utf8 $file.lang
done

# save a list of all currently available languages
echo "$files" > lang.txt

# go back to the original working directory
cd $dir
