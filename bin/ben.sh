#!/usr/bin/env bash
set -x

cur_size=4000000

for((i=1;i<=8;i++));
do
echo ${cur_size}
sh run.sh \
/data/mengqifeng/bench/tmp \
/data/mengqifeng/bench/out \
/data/mengqifeng/bench/input/10g.txt \
/data/mengqifeng/bench/input/10g2.txt \
${cur_size} 4
((cur_size=2*${cur_size}))
done