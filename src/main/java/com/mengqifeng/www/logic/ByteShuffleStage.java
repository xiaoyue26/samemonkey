package com.mengqifeng.www.logic;

import com.mengqifeng.www.utils.HashUtils;
import com.mengqifeng.www.utils.LogFactory;
import com.mengqifeng.www.utils.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ByteShuffleStage implements IStage {
    private final ApplicationContext context;
    private final Logger logger = LogFactory.getLogger(this.getClass());

    public ByteShuffleStage(ApplicationContext context) {
        this.context = context;
    }

    private final int writeBuffSize = 512 * 1024;
    private final int readBuffSize = 512 * 1024;

    @SuppressWarnings("Duplicates")
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

    private void writeLineWithIndex(
            List<BufferedOutputStream> printerList
            , byte[] buf
            , int left
            , int right
            , long rowIndex) throws IOException {
        int hashCode = HashUtils.hashCode(buf, left, right);
        int bucketId = Math.abs(hashCode) & context.bucketMask;

        BufferedOutputStream out = printerList.get(bucketId);
        if (left <= right) {
            out.write(buf, left, right - left + 1);
        } else {// 2215987
            logger.info("error");
            return;// TODO remove
        }
        out.write((context.SEP_STR
                + String.valueOf(rowIndex)
                + context.NL).getBytes(context.CS));
    }

    private void shuffle(int i) throws IOException {
        Path inFile = i == 0 ? context.inFile1 : context.inFile2;
        final Path workPath = i == 0 ? context.tmpPath1 : context.tmpPath2;
        final List<BufferedOutputStream> printerList = new ArrayList<>(context.bucketNum);
        for (int j = 0; j < context.bucketNum; j++) {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(Paths.get(workPath.toString()
                            , j + context.tmpPostFix).toFile())
                    , writeBuffSize);
            printerList.add(bos);
        }
        final byte[] buf = new byte[readBuffSize];
        final byte NL = (byte) '\n';
        int remainLen = 0;
        int left = 0, right = -1;
        try (InputStream is = Files.newInputStream(inFile)) {
            long rowIndex = 0;
            int len = is.read(buf, remainLen
                    , buf.length - remainLen);
            for (; len >= 0; len = is.read(buf, remainLen
                    , buf.length - remainLen)) {
                left = 0;
                right = remainLen - 1;
                // split buf with '\n'
                for (int j = remainLen; j < remainLen + len; j++) {
                    if (buf[j] == NL) {
                        if (rowIndex >= 717682) {
                            // System.out.println("debug");
                        }
                        if (left > right) {
                            System.out.println("error");
                        }
                        writeLineWithIndex(printerList, buf, left, right
                                , rowIndex);
                        left = j + 1;
                        right = j;
                        rowIndex++;
                    } else {
                        right++;
                    }
                }

                if (left <= right) {
                    // deal remaining bytes:
                    remainLen = right - left + 1;
                    // src,srcPos,dest,destPos,length:
                    System.arraycopy(buf, left, buf, 0, remainLen);
                } else {
                    remainLen = 0;
                }
            }
            if (remainLen > 0) {// deal last line
                writeLineWithIndex(printerList, buf, 0, remainLen - 1
                        , rowIndex);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            for (int j = 0; j < context.bucketNum; j++) {
                // PrintWriter out = printerList.get(j);
                BufferedOutputStream out = printerList.get(j);
                out.close();
            }
        }

    }

}