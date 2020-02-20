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

    public HashMergeWork(ConsoleParam param) {
        String epoch = String.valueOf(System.currentTimeMillis());
        // epoch = "1581843611465";
        Path tmpPath1 = Paths.get(param.tmpDir, epoch, "1");
        Path tmpPath2 = Paths.get(param.tmpDir, epoch, "2");
        Path outPath = Paths.get(param.outDir, epoch);
        Path t1 = Paths.get(param.inFile1);
        Path t2 = Paths.get(param.inFile2);
        Path inFile1, inFile2;
        if (t1.toFile().length()
                > t2.toFile().length()) { // 确保小的文件在前面:
            inFile1 = t2;
            inFile2 = t1;
        } else {
            inFile1 = t1;
            inFile2 = t2;
        }
        // 计算bucket数量:
        int bucketNum = getBucketNum(param.splitSize);
        logger.info("bucket_num: %d", bucketNum);
        int bucketMask = bucketNum - 1;
        context = new ApplicationContext(tmpPath1, tmpPath2
                , outPath, inFile1, inFile2
                , epoch, bucketNum, bucketMask);
        init_dirs();
    }

    private int getBucketNum(int splitSize) {
        long sumSize = context.inFile1.toFile().length()
                + context.inFile2.toFile().length();
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
        IStage shuffleStage = new ShuffleStage(context);
        shuffleStage.run();
        // 3. 读取tmp1、tmp2目录,依次merge n个文件,输出到out/{epoch}目录;
        IStage mergeStage = new MergeStage(context);
        mergeStage.run();
        // 4. clear资源
        clear();
    }


    private void clear() {
        // todo delete tmp files

        // todo delete out files
    }


}
