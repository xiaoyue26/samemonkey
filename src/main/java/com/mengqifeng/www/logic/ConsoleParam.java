package com.mengqifeng.www.logic;


public class ConsoleParam {
    public final String tmpDir;
    public final String outDir;
    public final String inFile1;
    public final String inFile2;
    public final int splitSize;
    public final int algoType;

    public ConsoleParam(String tmpDir
            , String outDir
            , String inFile1
            , String inFile2
            , int splitSize
            , int algoType) {
        this.tmpDir = tmpDir;
        this.outDir = outDir;
        this.inFile1 = inFile1;
        this.inFile2 = inFile2;
        this.splitSize = splitSize;
        this.algoType = algoType;
    }

    public static ConsoleParam parseArgs(String[] args) {
        System.out.println(args);
        if (args.length < 5) {
            throw new IllegalArgumentException("输入参数不足");
        }
        int algoType = 0;
        if (args.length >= 6) {
            algoType = Integer.valueOf(args[5]);
        }
        return new ConsoleParam(args[0], args[1], args[2], args[3]
                , Integer.valueOf(args[4])
                , algoType
        );
    }

    public static void main(String[] args) {

    }
}
