# The Same Monkey 真假美猴王
The Same Monkey(TSM) is a tool of finding the same row of two file.

寻找两个文件中相同的行，输出行号
# 编译
```bath
mvn package
```
# 运行
```bash
sh run.sh /data/mengqifeng/bench/input/10g.txt \
/data/mengqifeng/bench/input/10g2.txt \
/data/mengqifeng/bench/tmp \
/data/mengqifeng/bench/out \
16000000
```
参数含义依次为:
- 进行比较的第1个文件;
- 进行比较的第2个文件;
- 临时目录;(可选)
- 结果目录;(可选)
- 分片大小;(可选)
