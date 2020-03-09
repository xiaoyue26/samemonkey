#!/usr/bin/env bash
set -ex

nohup java -Xms2048m -Xmx2048m -classpath samemonkey-1.0-SNAPSHOT.jar \
Main /data/mengqifeng/bench/tmp \
/data/mengqifeng/bench/out \
/data/mengqifeng/bench/input/10g.txt \
/data/mengqifeng/bench/input/10g.txt \
16000000 > out.log 2>&1 &

free -h
df -h
cat /proc/cpuinfo| grep "processor"| wc -l
du -h /data/mengqifeng/bench/input/10g.txt
du -h /data/mengqifeng/bench/input/10gsame.txt
sh run.sh /data/mengqifeng/input/10g.txt /data/mengqifeng/input/10gsame.txt
