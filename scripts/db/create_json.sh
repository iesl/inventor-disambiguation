#!/usr/bin/env bash

json_file=$1

jarfile="target/inventor_disambiguation-1.0-SNAPSHOT-jar-with-dependencies.jar"

classname="edu.umass.cs.iesl.inventor_disambiguation.process.CreateInventorMentionJSON"

java -cp $jarfile -Xmx10G $classname --json-file $json_file \
--hostname=localhost \
--port=27972 \
--dbname=patentsview_data \
--collection-name=inventormentions \
--buffered-size=1000 \
--application-collection-name=application \
--assignee-collection-name=assignee \
--cpc-collection-name=cpc \
--inventor-collection-name=inventor \
--ipcr-collection-name=ipcr \
--lawyer-collection-name=lawyer \
--location-collection-name=location \
--nber-collection-name=nber \
--patent-collection-name=patent \
--us-patent-citation-collection-name=uspatentcitation \
--uspc-collection-name=uspc \
--inventor-file=data/rawinventor.csv \
--num-threads=20 \
--codec=ISO-8859-1