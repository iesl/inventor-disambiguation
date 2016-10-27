#!/bin/bash


input_training_file=$1 #The raw two column training data file
output_dir=$2 #Where to place all of the output
numthreads=${3:-18} #The number of threads


# Create the ouptut directory
mkdir -p $output_dir

# Copy the training file there
training_file="$output_dir/training_data"
cp $input_training_file $training_file


# Create the file of only ids
ids_file="${training_file}.ids_only"

cut -f 1 $training_file > $ids_file


# Generate the coref task file
jarpath="target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar"


# Create the coref tasks file:

echo "Creating the coref-tasks file"
START=$(date +%s)
START_TIME_CorefTasks=$(date +%x_%H:%M:%S:%N)

coref_tasks_file="${training_file}.coref_tasks"

java -Xmx40G -cp $jarpath edu.umass.cs.iesl.inventor_disambiguation.process.GenerateCorefTaskFile --inventor-file=data/rawinventor.csv \
--output-file=$coref_tasks_file \
--num-threads=20 \
--ids-file=$ids_file

END=$(date +%s)
END_TIME_CorefTasks=$(date +%x_%H:%M:%S:%N)

RTSECONDS_Coreftasks=$(($END - $START))
echo -e "Generate Coref Tasks Running Time (seconds) = $RTSECONDS_Coreftasks "


# Create the settings files
mkdir -p config/grid-search/
./scripts/grid-search/generate_settings.sh config/grid-search/

# Run the algorithms

echo "Running disambiguation algortihms"

START=$(date +%s)
START_TIME_Disambiguation=$(date +%x_%H:%M:%S:%N)

disambiguation_command="java -Xmx40G -cp $jarpath edu.umass.cs.iesl.inventor_disambiguation.coreference.RunParallelMultiCanopyCoreference --num-threads=$numthreads --coref-task-file=$coref_tasks_file --hostname=localhost --port=27972 --dbname=patentsview_data --collection-name=inventormentions --embedding-file=embeddings.txt"

for f in $(ls config/grid-search/); do
     $disambiguation_command --output-dir=$output_dir/results-$f $(cat config/grid-search/$f)
done

END=$(date +%s)
END_TIME_Disambiguation=$(date +%x_%H:%M:%S:%N)

RTSECONDS_Disambiguation=$(($END - $START))
echo -e "Disambiguation Running Time (seconds) = $RTSECONDS_Disambiguation"


## Score and Summarize

echo "Scoring the algorithms"
START=$(date +%s)
START_TIME_Scoring=$(date +%x_%H:%M:%S:%N)

score_command="java -Xmx30G -cp target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar edu.umass.cs.iesl.inventor_disambiguation.evaluation.ScoreFiles"

raw_summary_file="$output_dir/training-grid-search.txt"

for f in $(ls config/grid-search/); do
    $score_command $output_dir/results-$f/all-results.txt $training_file "$f" | grep  "summary" >> $raw_summary_file
done

summary_file="$output_dir/grid-search-summary.txt"
echo "Setting | PW Prec | PW Rec | PW F1 | MUC Prec | MUC Rec | MUC F1 | B3 Prec | B3 Rec | B3 F1 |" > $summary_file
cat $raw_summary_file| grep b3 | sort -k 5 -t "|" | cut -f 2- >> $summary_file


END_TIME_Scoring=$(date +%x_%H:%M:%S:%N)
END=$(date +%s)

RTSECONDS_Scoring=$(($END - $START))
echo -e "Scoring Running Time (seconds) = $RTSECONDS_Scoring"



# Start and end times
echo -e "Started Coref Tasks Generation at $START_TIME_CorefTasks"
echo -e "Ended Coref Tasks Generation at $END_TIME_CorefTasks"
echo -e "Started Disambiguation at $START_TIME_Disambiguation"
echo -e "Ended Disambiguation at $END_TIME_Disambiguation"
echo -e "Started Scoring at $START_TIME_Scoring"
echo -e "Ended Scoring at $END_TIME_Scoring"

echo -e "Generate Coref Tasks Running Time (seconds) = $RTSECONDS_Coreftasks "
echo -e "Disambiguation Running Time (seconds) = $RTSECONDS_Disambiguation"
echo -e "Scoring Running Time (seconds) = $RTSECONDS_Scoring"
