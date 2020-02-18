package com.mengqifeng.www.tools;

import java.util.Random;

public class ChineseGenerator {
    private final Random random;
    private final char begin = '\u4e00';
    private final char end = '\u9fa5';
    private final int randomBound = end - begin;

    public ChineseGenerator() {
        random = new Random(System.currentTimeMillis());
    }

    public final char getRandomChar() {
        return (char) (begin + random.nextInt(randomBound));
    }

    public final int fillRandomLine(char[] buf) {
        if (buf == null || buf.length < 2) {
            return 0;
        }
        int curLen = buf.length / 2
                + random.nextInt(buf.length / 2);
        for (int i = 0; i < curLen - 1; i++) {
            buf[i] = getRandomChar();
        }
        buf[curLen - 1] = '\n';
        return curLen;
    }

    public static void main(String[] args) {
        ChineseGenerator cg = new ChineseGenerator();
        for (int i = 0; i < 10; i++) {
            System.out.print(cg.getRandomChar());
        }
    }
}
