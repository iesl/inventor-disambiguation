#!/bin/sh

jarpath=target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar
command="java -Xmx20G -cp $jarpath edu.umass.cs.iesl.inventor_disambiguation.db.PopulateGeneralPatentDB"

START_TIME=$(date +%x_%H:%M:%S:%N)
START=$(date +%s)

$command --config=config/db/CreateAssignee.config --num-threads=20
$command --config=config/db/CreateCPC.config --num-threads=20
$command --config=config/db/CreateInventor.config --num-threads=20
$command --config=config/db/CreateIPCR.config --num-threads=20
$command --config=config/db/CreateLawyer.config --num-threads=20
java -Xmx20G -cp $jarpath edu.umass.cs.iesl.inventor_disambiguation.db.PopulateLocationDB --config=config/db/CreateLocation.config --num-threads=20
$command --config=config/db/CreateNBER.config --num-threads=20
$command --config=config/db/CreatePatent.config --num-threads=20
$command --config=config/db/CreateUSPC.config --num-threads=20

END=$(date +%s)
END_TIME=$(date +%x_%H:%M:%S:%N)
RTSECONDS=$(($END - $START))
echo -e "Populate Mongo DB Running Time (seconds) = $RTSECONDS "
echo -e "Started script at $START_TIME"
echo -e "Ended script at $END_TIME"