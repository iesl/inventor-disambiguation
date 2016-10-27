#!/bin/bash

START_TIME=$(date +%x_%H:%M:%S:%N)
START=$(date +%s)

wget http://www.dev.patentsview.org/api/bulk_downloads/rawassignee.zip -P data/
wget http://www.dev.patentsview.org/api/bulk_downloads/cpc_current.zip -P data/
wget http://www.dev.patentsview.org/api/bulk_downloads/rawinventor.zip -P data/
wget http://www.dev.patentsview.org/api/bulk_downloads/ipcr.zip -P data/
wget http://www.dev.patentsview.org/api/bulk_downloads/rawlawyer.zip -P data/
wget http://www.dev.patentsview.org/api/bulk_downloads/rawlocation.zip -P data/
wget http://www.dev.patentsview.org/api/bulk_downloads/nber.zip -P data/
wget http://www.dev.patentsview.org/api/bulk_downloads/patent.zip -P data/
wget http://www.dev.patentsview.org/api/bulk_downloads/uspc_current.zip -P data/

END=$(date +%s)
END_TIME=$(date +%x_%H:%M:%S:%N)
RTSECONDS=$(($END - $START))
echo -e "Running Time (seconds) = $RTSECONDS "
echo -e "Started script at $START_TIME"
echo -e "Ended script at $END_TIME"