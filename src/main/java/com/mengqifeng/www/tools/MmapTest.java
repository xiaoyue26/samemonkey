package com.mengqifeng.www.tools;

import sun.misc.Cleaner;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class MmapTest {

    static class MappedBiggerFileReader {
        private final MappedByteBuffer[] mappedBuffer;
        private final FileInputStream fileIn;
        private final long fileSize;
        private final int splitNum;
        private final int maxBufSize;

        private byte[] buffer;
        private int curSplitIndex = 0;

        public MappedBiggerFileReader(String fileName, int maxBufSize) throws IOException {
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

    private static void test1() throws IOException {
        for (int k = 0; k < 100; k++) {
            final int bufSize = 1024 << k;
            String filename = "D:/work/old/0604全量包/0604全量包.txt";
            MappedBiggerFileReader reader = new MappedBiggerFileReader(filename
                    , bufSize);
            long start = System.nanoTime();
            int count = 0;
            final byte NL = (byte) 10;
            while (reader.read() != -1) {
                byte[] buf = reader.getBuffer();
                // way1:
                for (int i = 0; i < buf.length; i++) { // 22s/128MB 22s/512MB
                    if (buf[i] == NL) {
                        count++;
                    }
                }
                // way2:
            /*BufferedReader in = new BufferedReader(new
                    InputStreamReader(new ByteArrayInputStream(buf)));
            // 42s:
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                count++;
            }*/
                // way3:
            /*String bigStr = new String(buf, StandardCharsets.UTF_8);
            for (int i = 0; i < bigStr.length(); i++) {// 30s/128MB 28s/512MB
                if (bigStr.charAt(i) == '\n') {
                    count++;
                }
            }*/

            }
            System.out.println(count); // 614090872
            long end = System.nanoTime();
            reader.close();
            System.out.println("MappedBiggerFileReader " + bufSize + ":" + (double) (end - start) / 1000000000 + "s");
        }
        // 28s
    }

    private static void test2() {
        for (int k = 0; k < 1; k++) {
            final int bufSize = 1024 << k;
            // final int bufSize = 262144;
            if (bufSize > 1024 * 1024 * 1024) break;
            long start = System.nanoTime();
            Path filePath = Paths.get("D:/work/old/0604全量包", "0604全量包.txt");
        /*SeekableByteChannel s = Files.newByteChannel(filePath);
        s.read(null);
        InputStream is = Files.newInputStream(filePath);
        is.read(null);*/

            // final int bufSize= 128*1024*1024;// 31s
            // final int bufSize= 128*1024; // 13s
            // final int bufSize= 65536; // 27~28s:
            // final int bufSize= 8196; // 28

            byte[] buf = new byte[bufSize];
            final byte NL = 10;
            int count = 0;
            try (InputStream is = Files.newInputStream(filePath)) {
                int len = is.read(buf);
                for (; len >= 0; ) {
                    for (int i = 0; i < len; i++) {
                        if (buf[i] == NL) count++;
                    }
                    len = is.read(buf);
                }
                System.out.println(count);
            } catch (IOException e) {
                e.printStackTrace();
            }
        /*try (Stream<String> lines = Files.lines(filePath)) {
            // System.out.println(lines.findFirst());
            System.out.println(lines.count());// 614090872
        } catch (IOException e) {
            e.printStackTrace();
        }*/
            long end = System.nanoTime();
            System.out.println("streams " + bufSize + ":" + (double) (end - start) / 1000000000 + "s");
        }
        // 35s~42s
    }

    public static void main(String[] args) throws IOException {
        // test1();//对mmap来说,bufSize影响不大
        test2(); // 对于普通read来说,bufSize影响很大
    }
}
