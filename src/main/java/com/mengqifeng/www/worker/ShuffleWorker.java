package com.mengqifeng.www.worker;

import com.mengqifeng.www.utils.BloomFilter;
import com.mengqifeng.www.utils.FileUtils;
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
 * 1. 读取第1个文件=>写到tmp/tmp_{epoch}/out1/n个文件夹;
 * 2. 读取第2个文件=>写到tmp/tmp_{epoch}/out2/n个文件夹;
 * 3. 读取out1、out2目录,依次merge n个文件夹,输出到out/{epoch}目录;
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
    private final String tmpFileName = "t1.tmp";
    private final String resFileName = "res.txt";

    public ShuffleWorker(String tmpDir, String outDir, String inFile1, String inFile2) {
        epoch = String.valueOf(System.currentTimeMillis()); // TODO 打开
        // epoch = "1581843611465";
        tmpPath1 = Paths.get(tmpDir, epoch, "1");
        tmpPath2 = Paths.get(tmpDir, epoch, "2");
        outPath = Paths.get(outDir, epoch);
        this.inFile1 = Paths.get(inFile1);
        this.inFile2 = Paths.get(inFile2);
        // 计算bucket数量:
        bucketNum = getBucketNum();
        init_dirs(bucketNum); // TODO 打开
    }

    private int getBucketNum() {
        return 128;
    }

    /**
     * init tmp dirs\out dirs
     * init n dir in tmp and out dir base on size of max(file1,file2)
     */
    private void init_dirs(int bucketNum) {
        Path tmp;
        for (int i = 0; i < bucketNum; i++) {
            tmp = Paths.get(tmpPath1.toString(), String.valueOf(i));
            if (!tmp.toFile().mkdirs()) {
                throw new RuntimeException("tmpDir invalid!");
            }
            tmp = Paths.get(tmpPath2.toString(), String.valueOf(i));
            if (!tmp.toFile().mkdirs()) {
                throw new RuntimeException("tmpDir invalid!");
            }
            tmp = Paths.get(outPath.toString(), String.valueOf(i));
            if (!tmp.toFile().mkdirs()) {
                throw new RuntimeException("outDir invalid!");
            }
        }
    }

    public void run() throws IOException {
        // 1. 读取第1个文件=>写到tmp/{epoch}/1/n个文件夹;
        shuffle(0); // todo 打开
        // 2. 读取第2个文件=>写到tmp/{epoch}/2/n个文件夹;
        shuffle(1); // todo 打开
        // 3. 读取tmp1、tmp2目录,依次merge n个文件夹,输出到out/{epoch}目录;
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
                    , String.valueOf(j)
                    , tmpFileName).toFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            printerList.add(out);
        }
        try (Stream<String> lines = Files.lines(inFile)) {
            Iterator<String> iterator = lines.iterator();
            long rowIndex = 0;
            while (iterator.hasNext()) {
                String line = iterator.next();
                int bucketId = Math.abs(line.hashCode()) % bucketNum;
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
        for (int i = 0; i < bucketNum; i++) {
            // 1. open tmp1-i build bloom+hashMap by tmp1
            final BloomFilter blf = new BloomFilter();
            final Map<String, List<Long>> map = new HashMap<>();
            Path tmpPath;
            tmpPath = Paths.get(tmpPath1.toString(), String.valueOf(i)
                    , tmpFileName);
            try (Stream<String> lines = Files.lines(tmpPath)) {
                lines.forEach(lineWithIndex -> {
                    String[] words = StringUtils.split(lineWithIndex,'\001');
                    List<Long> old = map.getOrDefault(words[0], null);
                    if (old == null) {
                        map.put(words[0], Arrays.asList(Long.valueOf(words[1])));
                    } else {
                        old.add(Long.valueOf(words[1]));
                    }
                    blf.add(words[0]);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 2 close tmp1
            // 3. open tmp2-i, write out-i
            tmpPath = Paths.get(tmpPath2.toString(), String.valueOf(i)
                    , tmpFileName);
            try (Stream<String> lines = Files.lines(tmpPath);
                 FileWriter fw = new FileWriter(Paths.get(outPath.toString()
                         , String.valueOf(i)
                         , tmpFileName).toFile(), true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)
            ) {
                lines.forEach(lineWithIndex -> {
                    String[] words = StringUtils.split(lineWithIndex,'\001');
                    if (blf.contains(words[0])) {
                        List<Long> old = map.getOrDefault(words[0], null);
                        if (old != null) {
                            for (Long index : old) {
                                out.write(words[0] + "\001" + index + "\001" + words[1]);
                            }
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 4. close file
            map.clear();
        }
        // 5. merge out:
        try (FileWriter fw = new FileWriter(Paths.get(outPath.toString()
                , resFileName).toFile(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            mergeFiles(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mergeFiles(PrintWriter out) {
        for (int i = 0; i < bucketNum; i++) {
            Path tmpPath = Paths.get(outPath.toString(), String.valueOf(i)
                    , tmpFileName);
            try (Stream<String> lines = Files.lines(tmpPath)) {
                lines.forEach(out::write);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void clear() {
        // todo delete tmp files

        // todo delete out1 files
        for (int i = 0; i < bucketNum; i++) {
            FileUtils.deleteDirectory(Paths.get(outPath.toString()
                    , String.valueOf(i)).toFile());
        }
    }


}
