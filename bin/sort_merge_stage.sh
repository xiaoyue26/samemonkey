#!/usr/bin/env bash
set -ex
# 176s
java -Xms2048m -Xmx2048m -classpath samemonkey-1.0-SNAPSHOT.jar \
Main /data/mengqifeng/bench/tmp \
/data/mengqifeng/bench/out \
/data/mengqifeng/bench/tmp/10g_nl_sort.txt \
/data/mengqifeng/bench/tmp/10gsame_nl_sort.txt \
256000000 3