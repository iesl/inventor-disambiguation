#!/usr/bin/env bash

json_file=$1
num_lines=$2

jarfile="target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar"

classname="edu.umass.cs.iesl.inventor_disambiguation.db.PopulateInventorMentionDBFromJSON"

if [[ -n $num_lines ]]; then
    java -cp $jarfile -Xmx10G $classname --json-file $json_file --num-lines $num_lines
else
    java -cp $jarfile -Xmx10G $classname --json-file $json_file
fi