# The Same Monkey
The Same Monkey(TSM) is a tool of finding the same row of two file.

# 编译
```bath
mvn package
```


# 运行
```bash
java -Xms2048m -Xmx2048m -classpath samemonkey-1.0-SNAPSHOT.jar \
Main /data/mengqifeng/bench/tmp /data/mengqifeng/bench/out /data/mengqifeng/bench/input/uin-45.txt /data/mengqifeng/bench/input/uin-45.txt 16000000 > out.log 2>&1 &
```
参数含义依次为:
临时目录;
结果目录;
进行比较的第1个文件;
进行比较的第2个文件;
分片大小;
