package com.mengqifeng.www.utils;

import sun.misc.Cleaner;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class MmapInStream implements Closeable {
    private final MappedByteBuffer[] mappedBuffer;
    private final FileInputStream fileIn;
    private final int splitNum;

    private int curSplitIndex = 0;
    private final int maxSplitSize = 128 * 1024 * 1024;// 128MB

    public MmapInStream(File file) throws IOException {
        fileIn = new FileInputStream(file);
        FileChannel fileChannel = fileIn.getChannel();
        final long fileSize = fileChannel.size();
        splitNum = (int) Math.ceil((double) fileSize / maxSplitSize);
        System.out.println("splitNum:" + splitNum);
        mappedBuffer = new MappedByteBuffer[splitNum];// splitNum个分片
        buildMmap(fileChannel, fileSize, splitNum, mappedBuffer);
    }

    private void buildMmap(FileChannel fileChannel
            , long fileSize, int splitNum
            , MappedByteBuffer[] mappedBuffer) throws IOException {
        long offset = 0;
        long splitSize;
        for (int i = 0; i < splitNum; i++) {
            long remained = fileSize - offset;
            if (remained < maxSplitSize) {
                splitSize = remained;
            } else {
                splitSize = maxSplitSize;
            }
            mappedBuffer[i] = fileChannel.map(FileChannel.MapMode.READ_ONLY
                    , offset
                    , splitSize);
            offset += splitSize;
        }
    }

    public int read(byte[] fillBuf, int offset, int length) {
        if (curSplitIndex >= splitNum) {
            return -1;
        }
        int remained = mappedBuffer[curSplitIndex].remaining();
        int readSize;
        if (remained > length) {
            readSize = length;
            mappedBuffer[curSplitIndex].get(fillBuf, offset, readSize);
        } else {
            readSize = remained;
            mappedBuffer[curSplitIndex].get(fillBuf, offset, readSize);
            curSplitIndex++;
        }
        return readSize;
    }

    public void close() throws IOException {
        fileIn.close();
        for (MappedByteBuffer mbb : mappedBuffer) {
            clean(mbb);
        }
    }

    @SuppressWarnings("Duplicates")
    public static void clean(final MappedByteBuffer mbb) {
        if (mbb == null) {
            return;
        }
        mbb.force();
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                Method getCleanerMethod = mbb.getClass().getMethod("cleaner", new Class[0]);
                getCleanerMethod.setAccessible(true);
                Cleaner cleaner = (Cleaner) getCleanerMethod.invoke(mbb, new Object[0]);
                cleaner.clean();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });


    }
}
