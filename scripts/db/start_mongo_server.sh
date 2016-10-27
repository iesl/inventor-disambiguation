#!/bin/sh


START_TIME=$(date +%x_%H:%M:%S:%N)
START=$(date +%s)

echo "Starting Mongo Server on port 27972 at $START_TIME"
mkdir -p data/mongodb/patentsview_data
numactl --interleave=all mongod --port 27972 --dbpath data/mongodb/patentsview_data


END=$(date +%s)
END_TIME=$(date +%x_%H:%M:%S:%N)
RTSECONDS=$(($END - $START))
echo -e "Running Time (seconds) = $RTSECONDS "
echo -e "Started script at $START_TIME"
echo -e "Ended script at $END_TIME"