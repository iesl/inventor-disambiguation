#!/bin/sh

wget http://www.dev.patentsview.org/workshop/data/benchmark_epfl.rar
wget http://www.dev.patentsview.org/workshop/data/benchmark_france.rar
mkdir benchmark_epfl
cd benchmark_epfl
unrar e ../benchmark_epfl.rar
cd ..
mkdir benchmark_france
cd benchmark_france
unrar e ../benchmark_france.rar
cd ..
