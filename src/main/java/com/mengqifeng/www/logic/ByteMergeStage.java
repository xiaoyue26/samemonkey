package com.mengqifeng.www.logic;

import com.mengqifeng.www.utils.FutureUtils;
import com.mengqifeng.www.utils.LogFactory;
import com.mengqifeng.www.utils.Logger;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class ByteMergeStage implements IMergeStage {
    private final Logger logger = LogFactory.getLogger(this.getClass());
    private final ApplicationContext context;
    private final int writeBuffSize = 512 * 1024;
    private final int readBuffSize = 512 * 1024;
    private final boolean useParallel;
    private final boolean useMmap;

    private int workingRow = 0;


    public ByteMergeStage(ApplicationContext context, boolean useMmap) {
        this.context = context;
        this.useMmap = useMmap;
        useParallel = false;
    }

    public ByteMergeStage(ApplicationContext context, boolean useMmap
            , boolean useParallel) {
        this.context = context;
        this.useMmap = useMmap;
        this.useParallel = useParallel;
    }

    private final int guessLineNum() {
        return (int) (context.inFile1.toFile().length()
                / 214 / context.bucketNum);
    }

    private void mergeIFile(int i) {
        logger.debug("begin merge tmp_%d:", i);
        // 1. open tmp1-i build bloom+hashMap by tmp1
        logger.debug("guessLineNum(): %d", guessLineNum());
        final Map<Node, List<Long>> map = new HashMap<>();
        Path tmpPath;
        // 选择较小的来build map:
        tmpPath = Paths.get(context.tmpPath1.toString()
                , String.valueOf(i) + context.tmpPostFix);
        final byte[] buf = new byte[readBuffSize];
        final byte NL = (byte) '\n';
        int remainLen = 0;
        int left = 0, right = -1;
        try (InputStream is = InputStreams.newInStream(tmpPath, useMmap)) {
            int len = is.read(buf, remainLen, buf.length - remainLen);
            for (; len >= 0; len = is.read(buf, remainLen, buf.length - remainLen)) {
                left = 0;
                right = remainLen - 1;
                // split buf with '\n'
                for (int j = remainLen; j < remainLen + len; j++) {
                    if (buf[j] == NL) {
                        recordLineWithIndex(map, buf, left, right);
                        left = j + 1;
                        right = j;
                        workingRow++;
                    } else {
                        right++;
                    }
                }
                if (left <= right) { // deal remaining bytes:
                    remainLen = right - left + 1;
                    // src,srcPos,dest,destPos,length:
                    System.arraycopy(buf, left, buf, 0, remainLen);
                } else {
                    remainLen = 0;
                }
            }
            if (remainLen > 0) {// deal last line
                recordLineWithIndex(map, buf, 0, remainLen - 1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        // 2. delete tmp1
        tmpPath.toFile().delete();
        logger.debug("build tmp1-%d info ok", i);
        logger.debug("tmp1-%d hashmap size: %d", i, map.size());
        // 3. open tmp2-i, write out-i
        tmpPath = Paths.get(context.tmpPath2.toString()
                , String.valueOf(i) + context.tmpPostFix);
        remainLen = 0;
        left = 0;
        right = -1;
        try (InputStream is = InputStreams.newInStream(tmpPath, useMmap);
             BufferedOutputStream out = new BufferedOutputStream(
                     new FileOutputStream(Paths.get(context.outPath.toString()
                             , String.valueOf(i) + context.tmpPostFix).toFile())
                     , writeBuffSize)
        ) {
            int len = is.read(buf, remainLen, buf.length - remainLen);
            for (; len >= 0; len = is.read(buf, remainLen, buf.length - remainLen)) {
                left = 0;
                right = remainLen - 1;
                // split buf with '\n'
                for (int j = remainLen; j < remainLen + len; j++) {
                    if (buf[j] == NL) {
                        writeLineWithIndex(out, map
                                , buf, left, right);
                        left = j + 1;
                        right = j;
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
                writeLineWithIndex(out, map
                        , buf, 0, remainLen - 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            tmpPath.toFile().delete();
        }
        // 4. close file
        logger.debug("finish merge tmp_%d.", i);
    }

    private Exception tryMergeIFile(int i) {
        try {
            mergeIFile(i);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }

    }

    public void mergeAndOut() throws IOException {
        logger.info("begin merge:");
        if (useParallel) {
            FutureUtils.submitAndCheck(0, context.bucketNum,
                    this::tryMergeIFile);
        } else {
            for (int i = 0; i < context.bucketNum; i++) {
                mergeIFile(i);
            }
        }


    }

    private void writeLineWithIndex(BufferedOutputStream out, Map<Node, List<Long>> map, byte[] buf, int left, int right) throws IOException {
        int i = right;
        byte sep = (byte) context.SEP;
        for (; i >= left; --i) {
            if (buf[i] == sep) break;
        }
        // [left,i-1] => line
        // [i+1,right] => rowIndex
        Node line = new Node(buf, left, i - 1);
        List<Long> old = map.get(line);
        if (old != null) {
            Long rowIndex = Long.valueOf(new String(buf
                    , i + 1, right - i, context.CS));
            for (Long index : old) {
                out.write(line.data);// write line
                if (context.reverseFlag) {// 文件1更大,先写rowIndex
                    out.write((context.SEP_STR + String.valueOf(rowIndex)
                            + context.SEP_STR + String.valueOf(index)
                            + context.NL).getBytes(context.CS));
                } else {// 文件1更小,先写map里的
                    out.write((context.SEP_STR + String.valueOf(index)
                            + context.SEP_STR + String.valueOf(rowIndex)
                            + context.NL).getBytes(context.CS));
                }

            }
        }
    }

    private void recordLineWithIndex(Map<Node, List<Long>> map, byte[] buf, int left, int right) {
        int i = right;
        byte sep = (byte) context.SEP;
        for (; i >= left; --i) {
            if (buf[i] == sep) break;
        }
        // [left,i-1] => line
        // [i+1,right] => rowIndex
        Node line;
        try {
            line = new Node(buf, left, i - 1);
            List<Long> old = map.get(line);
            Long rowIndex = Long.valueOf(new String(buf
                    , i + 1, right - i, context.CS));
            if (old == null) {
                old = new ArrayList<>();
                old.add(rowIndex);
                map.put(line, old);
            } else {
                old.add(rowIndex);
            }
        } catch (Throwable e) {
            logger.debug("error row index: %d", workingRow);
            logger.debug("left : %d", left);
            logger.debug("right : %d", right);
            logger.debug("i : %d", i);
            logger.debug("str: %s", new String(buf, left, right - left + 1));
            throw e;
        }

    }


}


