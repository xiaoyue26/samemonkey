#!/usr/bin/env bash
set -x
echo `date`
echo $#
JVM_HEAP=1024
if [ $# -lt 2 ] ; then
    # usage
    echo 'usage: sh run.sh <in_path1> <in_path2> [tmp_path] [out_path] [bufSize] [ALGO_TYPE]'
    exit
else
    IN_PATH1=$1
    IN_PATH2=$2
fi

if [ $# -lt 3 ] ; then
    TMP_PATH=./tmp
else
    TMP_PATH=$3
fi

if [ $# -lt 4 ] ; then
    OUT_PATH=./out
else
    OUT_PATH=$4
fi

if [ $# -lt 5 ] ; then
    BUF_SIZE=128000000
else
    BUF_SIZE=$5
fi

if [ $# -lt 6 ] ; then
    ALGO_TYPE=4
else
    ALGO_TYPE=$6
fi

java -Xms${JVM_HEAP}m -Xmx${JVM_HEAP}m -classpath samemonkey-1.0-SNAPSHOT.jar \
Main ${TMP_PATH} ${OUT_PATH} \
${IN_PATH1} ${IN_PATH2} \
${BUF_SIZE} ${ALGO_TYPE}
if [ $? -eq 0 ] ;then
    echo `date`
    echo 'job success'
    exit
else
    echo 'hash shuffle failed! try sort merge:'
fi
timestamp=`date +%Y%m%d_%H%M%S`
# 1.0 add line number:
echo `date`
awk '$0=NR"#"$0' ${IN_PATH1} > ${TMP_PATH}/${timestamp}_1.txt
if [ $? -eq 0 ] ;then
    echo `date`
else
    echo 'sort shuffle failed! give up...'
    rm ${TMP_PATH}/${timestamp}_1.txt
    exit
fi
# 1.1 sort first file:
LC_ALL=C \
sort -t '#' -k 2 --parallel=4 -S 2g -T ${TMP_PATH} \
${TMP_PATH}/${timestamp}_1.txt -o ${TMP_PATH}/${timestamp}_sort1.txt
if [ $? -eq 0 ] ;then
    echo `date`
    rm ${TMP_PATH}/${timestamp}_1.txt # limit rm
else
    echo 'sort shuffle failed! give up...'
    rm ${TMP_PATH}/${timestamp}_1.txt
    rm ${TMP_PATH}/${timestamp}_sort1.txt
    exit
fi
# 2.0 add line number:
echo `date`
awk '$0=NR"#"$0' ${IN_PATH2} > ${TMP_PATH}/${timestamp}_2.txt
if [ $? -eq 0 ] ;then
    echo `date`
else
    echo 'sort shuffle failed! give up...'
    rm ${TMP_PATH}/${timestamp}_2.txt
    exit
fi
# 2.1 sort second file:
LC_ALL=C \
sort -t '#' -k 2 --parallel=4 -S 2g -T ${TMP_PATH} \
${TMP_PATH}/${timestamp}_2.txt -o ${TMP_PATH}/${timestamp}_sort2.txt
if [ $? -eq 0 ] ;then
    echo `date`
    rm ${TMP_PATH}/${timestamp}_2.txt # limit rm
else
    echo 'sort shuffle failed! give up...'
    rm ${TMP_PATH}/${timestamp}_2.txt
    rm ${TMP_PATH}/${timestamp}_sort2.txt
    exit
fi
# 3. merge two sorted file:
java -Xms${JVM_HEAP}m -Xmx${JVM_HEAP}m -classpath samemonkey-1.0-SNAPSHOT.jar \
Main ${TMP_PATH} ${OUT_PATH} \
${TMP_PATH}/${timestamp}_sort1.txt \
${TMP_PATH}/${timestamp}_sort2.txt \
${BUF_SIZE} 2 # merge two files
if [ $? -eq 0 ] ;then
    echo `date`
    echo 'job success'
else
    echo 'sort shuffle failed! give up...'
fi
rm ${TMP_PATH}/${timestamp}_sort1.txt # limit rm
rm ${TMP_PATH}/${timestamp}_sort2.txt # limit rm
