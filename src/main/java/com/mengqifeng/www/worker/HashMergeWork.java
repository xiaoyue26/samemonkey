package com.mengqifeng.www.worker;

// import com.google.common.hash.BloomFilter;
// import com.google.common.hash.Funnels;

import com.mengqifeng.www.logic.*;
import com.mengqifeng.www.utils.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 执行计划:
 * 1. 读取第1个文件=>写到tmp/{epoch}/1/n个文件;
 * 2. 读取第2个文件=>写到tmp/{epoch}/2/n个文件;
 * 3. 读取上述目录,依次merge n个文件,输出到out/{epoch}目录;
 */
public class HashMergeWork implements IWorker {
    private final ApplicationContext context;
    private final Logger logger = LogFactory.getLogger(this.getClass());
    private final int algoType;
    private IShuffleStage shuffleStage;
    private IMergeStage mergeStage;

    public HashMergeWork(ConsoleParam param) {
        String epoch = String.valueOf(System.currentTimeMillis());
        // epoch = "1581843611465";
        Path tmpPath1 = Paths.get(param.tmpDir, epoch, "1");
        Path tmpPath2 = Paths.get(param.tmpDir, epoch, "2");
        Path outPath = Paths.get(param.outDir, epoch);
        Path t1 = Paths.get(param.inFile1);
        Path t2 = Paths.get(param.inFile2);
        Path inFile1, inFile2;
        boolean reverseFlag = false;
        if (t1.toFile().length()
                > t2.toFile().length()) { // 确保小的文件在前面:
            inFile1 = t2;
            inFile2 = t1;
            reverseFlag = true;
        } else {
            inFile1 = t1;
            inFile2 = t2;
        }
        // 计算bucket数量:
        int bucketNum = getBucketNum(param.splitSize,
                t1.toFile().length() + t2.toFile().length());
        logger.info("bucket_num: %d", bucketNum);
        int bucketMask = bucketNum - 1;
        context = new ApplicationContext(tmpPath1, tmpPath2
                , outPath, inFile1, inFile2
                , epoch, bucketNum, bucketMask, reverseFlag);
        init_dirs();
        algoType = param.algoType;
    }

    private int getBucketNum(int splitSize, long sumSize) {
        return SizeUtils.tableSizeFor((int) (sumSize / splitSize));
    }

    /**
     * init tmp dirs\out dirs
     * init n dir in tmp and out dir base on size of max(file1,file2)
     */
    private void init_dirs() {
        if (!context.tmpPath1.toFile().mkdirs()) {
            throw new RuntimeException("tmpDir invalid!");
        }
        if (!context.tmpPath2.toFile().mkdirs()) {
            throw new RuntimeException("tmpDir invalid!");
        }
        if (!context.outPath.toFile().mkdirs()) {
            throw new RuntimeException("outDir invalid!");
        }
        logger.info("init dir success.");
    }

    public void run() throws IOException {
        try {
            if (algoType == 0) {
                logger.info("using simple shuffle");
                shuffleStage = new ShuffleStage(context);
                mergeStage = new MergeStage(context);
            } else if (algoType == 1) {
                logger.info("using byte shuffle");
                shuffleStage = new ByteShuffleStage(context, false);
                mergeStage = new ByteMergeStage(context, false);
            } else if (algoType == 2) { // merge sorted:
                // merge two files:
                logger.info("merge two files");
                shuffleStage = new SortSuffleStage();
                mergeStage = new SortMergeStage(context);
            } else if (algoType == 3) {
                logger.info("using mmap shuffle");
                shuffleStage = new ByteShuffleStage(context, true);
                mergeStage = new ByteMergeStage(context, true);
            } else if (algoType == 4) {
                logger.info("using byte Parallel shuffle");
                shuffleStage = new ByteShuffleStage(
                        context, false, true);
                mergeStage = new ByteMergeStage(
                        context, false, true);
            } else if (algoType == 5) {
                logger.info("using byte Parallel merge");
                shuffleStage = new ByteShuffleStage(
                        context, false, false);
                mergeStage = new ByteMergeStage(
                        context, false, true);
            }
            shuffleStage.run();
            mergeStage.run();
        } catch (Throwable e) {
            throw e;// 接着往外抛
        } finally {
            // 4. clear资源
            clear();
        }
    }


    private void clear() {
        // tmp和out目录一定有拼上epoch字段,因此不会误删根目录:
        // clear tmp path:
        FileUtils.deleteDirectory(context.tmpPath1.toFile());
        FileUtils.deleteDirectory(context.tmpPath2.toFile());
        // delete out path:
        // FileUtils.deleteDirectory(context.outPath.toFile());
    }


}
