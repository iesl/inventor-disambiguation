#!/bin/sh

START_TIME=$(date +%x_%H:%M:%S:%N)
START=$(date +%s)

java -Xmx40G -cp target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar edu.umass.cs.iesl.inventor_disambiguation.db.CreateInventorMentionDB --config=config/db/CreateInventorMention.config

END=$(date +%s)
END_TIME=$(date +%x_%H:%M:%S:%N)
RTSECONDS=$(($END - $START))
echo -e "Generate Inventor Mentions Running Time (seconds) = $RTSECONDS "
echo -e "Started script at $START_TIME"
echo -e "Ended script at $END_TIME"