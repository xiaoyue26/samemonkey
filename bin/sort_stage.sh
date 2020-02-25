#!/usr/bin/env bash
set -ex

# file1:
# 1min18:
date && awk '$0=NR"#"$0' input/10g.txt > /data/mengqifeng/bench/tmp/10g_nl.txt && date

# 2min26~38:
date && LC_ALL=C \
sort -t '#' -k 2 --parallel=4 -S 2g -T /data/mengqifeng/bench/tmp \
/data/mengqifeng/bench/tmp/10g_nl.txt -o /data/mengqifeng/bench/tmp/10g_nl_sort.txt
date

rm /data/mengqifeng/bench/tmp/10g_nl.txt

# file2:
# 1min16:
date && awk '$0=NR"#"$0' input/10gsame.txt > /data/mengqifeng/bench/tmp/10gsame_nl.txt && date

# 2min32:
date && LC_ALL=C \
sort -t '#' -k 2  --parallel=4 -S 2g -T /data/mengqifeng/bench/tmp \
/data/mengqifeng/bench/tmp/10gsame_nl.txt -o /data/mengqifeng/bench/tmp/10gsame_nl_sort.txt
date

rm /data/mengqifeng/bench/tmp/10gsame_nl.txt



