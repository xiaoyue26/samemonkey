package com.mengqifeng.www.tools;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.mengqifeng.www.utils.MmapReader;

public class MmapTest {

    private static void test1() throws IOException {
        for (int k = 0; k < 100; k++) {
            final int bufSize = 1024 << k;
            String filename = "D:/work/old/0604全量包/0604全量包.txt";
            MmapReader reader = new MmapReader(filename
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
            System.out.println("MmapReader " + bufSize + ":" + (double) (end - start) / 1000000000 + "s");
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
