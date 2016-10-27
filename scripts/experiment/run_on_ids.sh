#!/bin/sh

ids_file=$1
output_dir=$2
numthreads=${3:-18}

jarpath="target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar"

mkdir -p $output_dir

# Create the coref tasks file:


echo "Creating the coref-tasks file"
START=$(date +%s)
START_TIME_CorefTasks=$(date +%x_%H:%M:%S:%N)


java -Xmx40G -cp $jarpath edu.umass.cs.iesl.inventor_disambiguation.process.GenerateCorefTaskFile --inventor-file=data/rawinventor.csv \
--output-file=$output_dir/coref-tasks-ids.tsv \
--num-threads=20 \
--ids-file=$ids_file

END=$(date +%s)
END_TIME_CorefTasks=$(date +%x_%H:%M:%S:%N)

RTSECONDS_Coreftasks=$(($END - $START))
echo -e "Generate Coref Tasks Running Time (seconds) = $RTSECONDS_Coreftasks "


# Running the disambiguation algorithm
echo "Running disambiguation"

START=$(date +%s)
START_TIME_Disambiguation=$(date +%x_%H:%M:%S:%N)

java -Xmx40G -cp $jarpath edu.umass.cs.iesl.inventor_disambiguation.coreference.RunParallelMultiCanopyCoreference --num-threads=$numthreads --coref-task-file=$output_dir/coref-tasks-ids.tsv --output-dir=$output_dir/results $(cat config/coref/StandardCorefConfig.config)

END=$(date +%s)
END_TIME_Disambiguation=$(date +%x_%H:%M:%S:%N)
RTSECONDS_Disambiguation=$(($END - $START))

echo -e "Generate Coref Tasks Running Time (seconds) = $RTSECONDS_Coreftasks "
echo -e "Started Coref Tasks script at $START_TIME_CorefTasks"
echo -e "Ended Coref Tasks script at $END_TIME_CorefTasks"
echo -e "Run Multi Canopy Coref Running Time (seconds) = $RTSECONDS_Disambiguation "
echo -e "Started Disambiguation script at $START_TIME_Disambiguation"
echo -e "Ended Disambiguation script at $END_TIME_Disambiguation"

