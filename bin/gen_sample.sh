nohup java -Xms2048m -Xmx2048m -classpath samemonkey-1.0-SNAPSHOT.jar \
com.mengqifeng.www.tools.SampleGenerator \
/data/mengqifeng/input/sample3.txt \
20000000 \
/data/mengqifeng/input/dict1.txt \
> gen.log 2>&1 &