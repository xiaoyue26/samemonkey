package com.mengqifeng.www.utils;

import sun.misc.Cleaner;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class MmapReader {
    private final MappedByteBuffer[] mappedBuffer;
    private final FileInputStream fileIn;
    private final long fileSize;
    private final int splitNum;
    private final int maxBufSize;

    private byte[] buffer;
    private int curSplitIndex = 0;

    public MmapReader(String fileName, int maxBufSize) throws IOException {
        this.maxBufSize = maxBufSize;
        fileIn = new FileInputStream(fileName);
        FileChannel fileChannel = fileIn.getChannel();
        fileSize = fileChannel.size();
        int maxSplitSize = 128 * 1024 * 1024;// 128MB
        splitNum = (int) Math.ceil((double) fileSize / maxSplitSize);
        System.out.println("splitNum:" + splitNum);
        mappedBuffer = new MappedByteBuffer[splitNum];// splitNum个分片
        buildMmap(fileChannel, fileSize, splitNum, mappedBuffer, maxSplitSize);
    }

    private void buildMmap(FileChannel fileChannel
            , long fileSize, int splitNum
            , MappedByteBuffer[] mappedBuffer
            , int maxSplitSize) throws IOException {
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

    public int read() {
        if (curSplitIndex >= splitNum) {
            return -1;
        }
        int remained = mappedBuffer[curSplitIndex].remaining();
        int readSize;
        if (remained > maxBufSize) {
            readSize = maxBufSize;
            buffer = new byte[readSize];
            mappedBuffer[curSplitIndex].get(buffer);
            // mappedBuffer[curSplitIndex].flip();
            // mappedBuffer[curSplitIndex].rewind();
        } else {
            readSize = remained;
            buffer = new byte[readSize];
            mappedBuffer[curSplitIndex].get(buffer);
            curSplitIndex++;
        }
        return readSize;
    }

    public void close() throws IOException {
        fileIn.close();
        buffer = null;
    }

    public byte[] getBuffer() {
        return buffer;
    }


    public static void clean(final MappedByteBuffer mbb) throws Exception {
        if (mbb == null) {
            return;
        }
        mbb.force();
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            try {
                // System.out.println(buffer.getClass().getName());
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
