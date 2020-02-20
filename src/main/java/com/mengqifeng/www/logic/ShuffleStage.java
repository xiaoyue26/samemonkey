package com.mengqifeng.www.logic;

import com.mengqifeng.www.utils.LogFactory;
import com.mengqifeng.www.utils.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class ShuffleStage implements IStage{
    private final ApplicationContext context;
    private final Logger logger = LogFactory.getLogger(this.getClass());

    public ShuffleStage(ApplicationContext context) {
        this.context = context;
    }

    public void run() throws IOException {
        logger.info("shuffling first file:");
        // 1. 读取第1个文件=>写到tmp/{epoch}/1/n个文件;
        shuffle(0);
        logger.info("finish shuffle first file.");
        // 2. 读取第2个文件=>写到tmp/{epoch}/2/n个文件;
        logger.info("shuffling second file:");
        shuffle(1);
        logger.info("finish shuffle second file.");

    }

    private void shuffle(int i) throws IOException {
        Path inFile = i == 0 ? context.inFile1 : context.inFile2;
        final Path workPath = i == 0 ? context.tmpPath1 : context.tmpPath2;
        final List<PrintWriter> printerList = new ArrayList<>(context.bucketNum);
        for (int j = 0; j < context.bucketNum; j++) {
            FileWriter fw = new FileWriter(Paths.get(workPath.toString()
                    , j + context.tmpPostFix).toFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);
            printerList.add(out);
        }
        try (Stream<String> lines = Files.lines(inFile, context.CS)) {
            Iterator<String> iterator = lines.iterator();
            long rowIndex = 0;
            while (iterator.hasNext()) {
                String line = iterator.next();
                int bucketId = Math.abs(line.hashCode()) & context.bucketMask;
                PrintWriter out = printerList.get(bucketId);
                out.write(line + context.SEP_STR + rowIndex + context.NL);
                rowIndex++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int j = 0; j < context.bucketNum; j++) {
                PrintWriter out = printerList.get(j);
                out.close();
            }
        }

    }

}
