package com.mengqifeng.www.tools;

import java.util.List;
import java.util.Random;

public class SameRowGenerator implements ICharGenerator {
    private final Random random;
    private volatile char[] sameBuf;
    private final List<Character> dictList;

    public SameRowGenerator(List<Character> dictList) {
        random = new Random(System.currentTimeMillis());
        this.dictList = dictList;
    }

    @Override
    public char getRandomChar() {
        return dictList.get(nextInt(dictList.size()));
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

    public int fillRandomLine(char[] buf) {
        if (buf == null || buf.length < 2) {
            return 0;
        }
        if (sameBuf == null) {
            int curLen = buf.length / 2
                    + nextInt(buf.length / 2);
            sameBuf = new char[curLen];
            for (int i = 0; i < curLen - 1; i++) {
                sameBuf[i] = getRandomChar();
            }
            sameBuf[curLen - 1] = '\n';
        }
        for (int i = 0; i < sameBuf.length; i++) {
            buf[i] = sameBuf[i];
        }
        return sameBuf.length;

    }
}
