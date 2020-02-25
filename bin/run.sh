#!/usr/bin/env bash
set -x
echo `date`
echo $#
if [ $# -lt 4 ] ; then
    # usage
    echo 'usage: sh run.sh <tmp_path> <out_path> <in_path1> <in_path2> [bufSize]'
    exit
else
    TMP_PATH=$1
    OUT_PATH=$2
    IN_PATH1=$3
    IN_PATH2=$4
fi

if [ $# -lt 5 ] ; then
    BUF_SIZE=128000000
else
    BUF_SIZE=$5
fi

java -Xms2048m -Xmx2048m -classpath samemonkey-1.0-SNAPSHOT.jar \
Main ${TMP_PATH} ${OUT_PATH} \
${IN_PATH1} ${IN_PATH2} \
${BUF_SIZE} 1

if [ $? -eq 0 ] ;then
    echo `date`
    echo 'job success'
    exit
else
    echo 'hash shuffle failed! try sort merge:'
fi

timestamp=`date +%Y%m%d_%H%M%S`

echo `date`
awk '$0=NR"#"$0' ${IN_PATH1} > ${TMP_PATH}/${timestamp}_1.txt
echo `date`

LC_ALL=C \
sort -t '#' -k 2 --parallel=4 -S 2g -T ${TMP_PATH} \
${TMP_PATH}/${timestamp}_1.txt -o ${TMP_PATH}/${timestamp}_sort1.txt
echo `date`

rm ${TMP_PATH}/${timestamp}_1.txt # limit rm


echo `date`
awk '$0=NR"#"$0' ${IN_PATH2} > ${TMP_PATH}/${timestamp}_2.txt
echo `date`

LC_ALL=C \
sort -t '#' -k 2 --parallel=4 -S 2g -T ${TMP_PATH} \
${TMP_PATH}/${timestamp}_2.txt -o ${TMP_PATH}/${timestamp}_sort2.txt
echo `date`

rm ${TMP_PATH}/${timestamp}_2.txt # limit rm


java -Xms2048m -Xmx2048m -classpath samemonkey-1.0-SNAPSHOT.jar \
Main ${TMP_PATH} ${OUT_PATH} \
${TMP_PATH}/${timestamp}_sort1.txt \
${TMP_PATH}/${timestamp}_sort1.txt \
${BUF_SIZE} 2


if [ $? -eq 0 ] ;then
    echo `date`
    echo 'job success'
    exit
else
    echo 'sort shuffle failed! give up...'
fi