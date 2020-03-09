package com.mengqifeng.www.logic;


import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ApplicationContext {

    // 临时工作目录:
    public final Path tmpPath1;
    public final Path tmpPath2;
    // 输出文件夹:
    public final Path outPath;
    // 输入文件路径:
    public final Path inFile1; // smaller file
    public final Path inFile2;
    // long epoch;
    public final String epoch;
    public final int bucketNum;
    public final int bucketMask;
    public final String tmpPostFix = ".txt";
    // public final String resFileName = "res.txt";

    public final Charset CS = StandardCharsets.UTF_8;
    public final char SEP = '#';
    public final String SEP_STR = "#";
    public final String NL = "\n";
    public final boolean reverseFlag;// 是否交换过两个文件的顺序


    public ApplicationContext(Path tmpPath1
            , Path tmpPath2
            , Path outPath
            , Path inFile1
            , Path inFile2
            , String epoch, int bucketNum, int bucketMask
            , boolean reverseFlag) {
        this.tmpPath1 = tmpPath1;
        this.tmpPath2 = tmpPath2;
        this.outPath = outPath;
        this.inFile1 = inFile1;
        this.inFile2 = inFile2;
        this.epoch = epoch;
        this.bucketNum = bucketNum;
        this.bucketMask = bucketMask;
        this.reverseFlag = reverseFlag;
    }
}
