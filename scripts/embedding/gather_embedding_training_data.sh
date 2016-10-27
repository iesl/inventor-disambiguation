#!/bin/sh


echo "Gathering training data for the word embeddings"

START_TIME=$(date +%x_%H:%M:%S:%N)
START=$(date +%s)

mkdir -p data/embedding/

java -Xmx40G -cp target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar \
  edu.umass.cs.iesl.inventor_disambiguation.process.GatherEmbeddingTrainingData \
 --patent-file=data/patent.csv \
 --num-threads=20 \
 --output=data/embedding/training-data.txt
 
 
END=$(date +%s)
END_TIME=$(date +%x_%H:%M:%S:%N)
RTSECONDS=$(($END - $START))
echo -e "Gather training data, Running Time (seconds) = $RTSECONDS "
echo -e "Started script at $START_TIME"
echo -e "Ended script at $END_TIME"

