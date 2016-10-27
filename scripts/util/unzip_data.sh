#!/bin/bash

START_TIME=$(date +%x_%H:%M:%S:%N)
START=$(date +%s)

unzip data/rawassignee.zip -d data/
unzip data/cpc_current.zip -d data/
unzip data/rawinventor.zip -d data/
unzip data/ipcr.zip -d data/
unzip data/rawlawyer.zip -d data/
unzip data/rawlocation.zip -d data/
unzip data/nber.zip -d data/
unzip data/patent.zip -d data/
unzip data/uspc_current.zip -d data/

END=$(date +%s)
END_TIME=$(date +%x_%H:%M:%S:%N)
RTSECONDS=$(($END - $START))
echo -e "Running Time (seconds) = $RTSECONDS "
echo -e "Started script at $START_TIME"
echo -e "Ended script at $END_TIME"