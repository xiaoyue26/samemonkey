package com.mengqifeng.www.worker;

import com.mengqifeng.www.logic.ConsoleParam;
import com.mengqifeng.www.utils.Logger;
import com.mengqifeng.www.utils.OpenHashMap;
import com.mengqifeng.www.utils.SizeUtils;
import com.mengqifeng.www.utils.StringUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * 执行计划:
 * 1. 读取第1个文件=>写到tmp/{epoch}/1/n个文件;
 * 2. 读取第2个文件=>写到tmp/{epoch}/2/n个文件;
 * 3. 读取上述目录,依次merge n个文件,输出到out/{epoch}目录;
 */
public class ShuffleWorker implements IWorker {
    // 临时工作目录:
    private final Path tmpPath1;
    private final Path tmpPath2;
    // 输出文件夹:
    private final Path outPath;
    // 输入文件路径:
    private final Path inFile1;
    private final Path inFile2;
    // long epoch;
    private final String epoch;
    private final int bucketNum;
    private final int bucketMask;
    private final String tmpPostFix = ".txt";
    private final String resFileName = "res.txt";
    private static final Logger logger = new Logger();


    public ShuffleWorker(ConsoleParam param) {
        epoch = String.valueOf(System.currentTimeMillis()); // TODO 打开
        // epoch = "1581843611465";
        tmpPath1 = Paths.get(param.tmpDir, epoch, "1");
        tmpPath2 = Paths.get(param.tmpDir, epoch, "2");
        outPath = Paths.get(param.outDir, epoch);
        this.inFile1 = Paths.get(param.inFile1);
        this.inFile2 = Paths.get(param.inFile2);
        // 计算bucket数量:
        bucketNum = getBucketNum(param.splitSize);
        bucketMask = bucketNum - 1;
        init_dirs(); // TODO 打开
    }

    private int getBucketNum(int splitSize) {
        long sumSize = inFile1.toFile().length()
                + inFile2.toFile().length();
        return SizeUtils.tableSizeFor((int) (sumSize / splitSize));
    }

    /**
     * init tmp dirs\out dirs
     * init n dir in tmp and out dir base on size of max(file1,file2)
     */
    private void init_dirs() {
        if (!tmpPath1.toFile().mkdirs()) {
            throw new RuntimeException("tmpDir invalid!");
        }
        if (!tmpPath2.toFile().mkdirs()) {
            throw new RuntimeException("tmpDir invalid!");
        }
        if (!outPath.toFile().mkdirs()) {
            throw new RuntimeException("outDir invalid!");
        }
        logger.info("init dir success.");
    }

    public void run() throws IOException {
        logger.info("shuffling first file:");
        // 1. 读取第1个文件=>写到tmp/{epoch}/1/n个文件;
        shuffle(0); // todo 打开
        logger.info("finish shuffle first file.");
        // 2. 读取第2个文件=>写到tmp/{epoch}/2/n个文件;
        logger.info("shuffling second file:");
        shuffle(1); // todo 打开
        logger.info("finish shuffle second file.");
        // 3. 读取tmp1、tmp2目录,依次merge n个文件,输出到out/{epoch}目录;
        mergeAndOut();
        // 4. clear资源
        clear();
    }


    private void shuffle(int i) throws IOException {
        Path inFile = i == 0 ? inFile1 : inFile2;
        final Path workPath = i == 0 ? tmpPath1 : tmpPath2;
        final List<PrintWriter> printerList = new ArrayList<>(bucketNum);
        for (int j = 0; j < bucketNum; j++) {
            FileWriter fw = new FileWriter(Paths.get(workPath.toString()
                    , j + tmpPostFix).toFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            printerList.add(out);
        }
        try (Stream<String> lines = Files.lines(inFile)) {
            Iterator<String> iterator = lines.iterator();
            long rowIndex = 0;
            while (iterator.hasNext()) {
                String line = iterator.next();
                int bucketId = Math.abs(line.hashCode()) & bucketMask;
                PrintWriter out = printerList.get(bucketId);
                out.write(line + "\001" + rowIndex + "\n");
                rowIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int j = 0; j < bucketNum; j++) {
                PrintWriter out = printerList.get(j);
                out.close();
            }
        }

    }

    private void mergeAndOut() {
        logger.info("begin merge:");
        for (int i = 0; i < bucketNum; i++) {
            logger.info("begin merge tmp_%d:", i);
            // 1. open tmp1-i build bloom+hashMap by tmp1
            // final StringBloomFilter blf = new StringBloomFilter();
            final Map<String, List<Long>> map = new HashMap<>();
            // final Map<String, List<Long>> map = new OpenHashMap<>();
            Path tmpPath;
            tmpPath = Paths.get(tmpPath1.toString()
                    , String.valueOf(i) + tmpPostFix);
            try (Stream<String> lines = Files.lines(tmpPath)) {

                lines.forEach(lineWithIndex -> {
                    String[] words = StringUtils.split(lineWithIndex, '\001');
                    List<Long> old = map.get(words[0]);
                    if (old == null) {
                        map.put(words[0], Arrays.asList(Long.valueOf(words[1])));
                    } else {
                        old.add(Long.valueOf(words[1]));
                    }
                    // blf.add(words[0]);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 2. delete tmp1
            tmpPath.toFile().delete();
            logger.info("build tmp1-%d info ok", i);
            logger.info("tmp1-%d hashmap size: %d", i, map.size());
            // 3. open tmp2-i, write out-i
            tmpPath = Paths.get(tmpPath2.toString()
                    , String.valueOf(i) + tmpPostFix);
            try (Stream<String> lines = Files.lines(tmpPath);
                 FileWriter fw = new FileWriter(Paths.get(outPath.toString()
                         , String.valueOf(i) + tmpPostFix).toFile()
                         , true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)
            ) {
                lines.forEach(lineWithIndex -> {
                    String[] words = StringUtils.split(lineWithIndex, '\001');
                    // if (blf.contains(words[0])) {
                    List<Long> old = map.getOrDefault(words[0], null);
                    if (old != null) {
                        for (Long index : old) {
                            out.write(words[0] + "\001" + index + "\001" + words[1] + '\n');
                        }
                    }
                    // }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // map = null;
                tmpPath.toFile().delete();
            }
            // 4. close file
            logger.info("finish merge tmp_%d.", i);
        }
        // 5. merge out:
        logger.info("begin merge res:");
        try (FileWriter fw = new FileWriter(Paths.get(outPath.toString()
                , resFileName).toFile(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            // mergeFiles(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mergeFiles(PrintWriter out) {
        for (int i = 0; i < bucketNum; i++) {
            Path tmpPath = Paths.get(outPath.toString()
                    , String.valueOf(i) + tmpPostFix);
            try (Stream<String> lines = Files.lines(tmpPath)) {
                lines.forEach(
                        line -> {
                            out.write(line + '\n');
                        }
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void clear() {
        // todo delete tmp files

        // todo delete out1 files
    }


}
