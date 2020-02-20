nohup java -Xms2048m -Xmx2048m -classpath samemonkey-1.0-SNAPSHOT.jar \
com.mengqifeng.www.tools.SampleGenerator \
/data/mengqifeng/input/sample3.txt \
20000000 \
/data/mengqifeng/input/dict1.txt \
> gen.log 2>&1 &

# 生成10G:
java -Xms2048m -Xmx2048m -classpath samemonkey-1.0-SNAPSHOT.jar \
com.mengqifeng.www.tools.SampleGenerator \
/data/mengqifeng/bench/input/10g.txt \
50000000 \
/data/mengqifeng/bench/input/dict1.txt

java -Xms2048m -Xmx2048m -classpath samemonkey-1.0-SNAPSHOT.jar \
com.mengqifeng.www.tools.SampleGenerator \
/data/mengqifeng/bench/input/10g2.txt \
50000000 \
/data/mengqifeng/bench/input/dict1.txt