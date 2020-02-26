# The Same Monkey
The Same Monkey(TSM) is a tool of finding the same row of two file.
真假美猴王
寻找两个文件中相同的行，输出行号

# 编译
```bath
mvn package
```


# 运行
```bash
sh run.sh /data/mengqifeng/bench/tmp \
/data/mengqifeng/bench/out \
/data/mengqifeng/bench/input/10g.txt \
/data/mengqifeng/bench/input/10g2.txt \
16000000
```
参数含义依次为:
- 临时目录;
- 结果目录;
- 进行比较的第1个文件;
- 进行比较的第2个文件;
- 分片大小;(可选)
