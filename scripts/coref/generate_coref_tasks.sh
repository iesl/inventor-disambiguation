#!/bin/sh

jarpath="target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar"

START_TIME=$(date +%x_%H:%M:%S:%N)
START=$(date +%s)

java -Xmx40G -cp $jarpath edu.umass.cs.iesl.inventor_disambiguation.process.GenerateCorefTaskFile --config=config/coref/GenerateCorefTasks.config

END=$(date +%s)
END_TIME=$(date +%x_%H:%M:%S:%N)
RTSECONDS=$(($END - $START))
echo -e "Generate Coref Tasks Running Time (seconds) = $RTSECONDS "
echo -e "Started script at $START_TIME"
echo -e "Ended script at $END_TIME"