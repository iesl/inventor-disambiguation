#!/bin/sh

jarpath="target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar"
config_file=${1:-config/coref/RunConfig.config}
weights_file=${2:-config/coref/WeightsCommonCharacteristics.config}

echo "Config file: $config_file"
echo "Weights file: $weights_file"

START_TIME=$(date +%x_%H:%M:%S:%N)
START=$(date +%s)

java -Xmx40G -cp $jarpath edu.umass.cs.iesl.inventor_disambiguation.coreference.RunParallelMultiCanopyCoreference $(cat $config_file) $(cat $weights_file)

END=$(date +%s)
END_TIME=$(date +%x_%H:%M:%S:%N)
RTSECONDS=$(($END - $START))
echo -e "Run Multi Canopy Coref Running Time (seconds) = $RTSECONDS "
echo -e "Started script at $START_TIME"
echo -e "Ended script at $END_TIME"