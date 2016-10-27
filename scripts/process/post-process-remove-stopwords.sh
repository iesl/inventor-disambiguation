#!/bin/bash

jarpath="target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar"

input=$1
output=$2
codec=${3:-"UTF-8"}

START_TIME=$(date +%x_%H:%M:%S:%N)
START=$(date +%s)

java -Xmx6G -cp $jarpath edu.umass.cs.iesl.inventor_disambiguation.process.PostProcessRemoveStopwords --input $input --output $output --codec $codec

END=$(date +%s)
END_TIME=$(date +%x_%H:%M:%S:%N)
RTSECONDS=$(($END - $START))