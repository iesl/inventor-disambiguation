#!/bin/bash

START_TIME=$(date +%x_%H:%M:%S:%N)
START=$(date +%s)

wget http://www.patentsview.org/data/2015/rawassignee.zip -P data/
wget http://www.patentsview.org/data/2015/cpc_current.zip -P data/
wget http://www.patentsview.org/data/2015/rawinventor.zip -P data/
wget http://www.patentsview.org/data/2015/ipcr.zip -P data/
wget http://www.patentsview.org/data/2015/rawlawyer.zip -P data/
wget http://www.patentsview.org/data/2015/rawlocation.zip -P data/
wget http://www.patentsview.org/data/2015/nber.zip -P data/
wget http://www.patentsview.org/data/2015/patent.zip -P data/
wget http://www.patentsview.org/data/2015/uspc_current.zip -P data/

END=$(date +%s)
END_TIME=$(date +%x_%H:%M:%S:%N)
RTSECONDS=$(($END - $START))
echo -e "Running Time (seconds) = $RTSECONDS "
echo -e "Started script at $START_TIME"
echo -e "Ended script at $END_TIME"