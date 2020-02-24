#!/usr/bin/env bash
set -ex

nohup java -Xms2048m -Xmx2048m -classpath samemonkey-1.0-SNAPSHOT.jar \
Main /data/mengqifeng/bench/tmp \
/data/mengqifeng/bench/out \
/data/mengqifeng/bench/input/10g.txt \
/data/mengqifeng/bench/input/10g.txt \
16000000 > out.log 2>&1 &