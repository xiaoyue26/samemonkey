package com.mengqifeng.www.logic;

public class WorkerParam {
    public final String tmpDir;
    public final String outDir;
    public final String inFile1;
    public final String inFile2;

    public WorkerParam(String tmpDir
            , String outDir
            , String inFile1
            , String inFile2) {
        this.tmpDir = tmpDir;
        this.outDir = outDir;
        this.inFile1 = inFile1;
        this.inFile2 = inFile2;
    }

    public static WorkerParam parseArgs(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("输入参数不足");
        }
        return new WorkerParam(args[0], args[1], args[2], args[3]);
    }

    public static void main(String[] args) {

    }
}
