package com.mengqifeng.www.tools;

import java.util.Random;

public interface ICharGenerator {
    char getRandomChar() ;
    int nextInt(int bound);
    default int fillRandomLine(char[] buf) {
        if (buf == null || buf.length < 2) {
            return 0;
        }
        int curLen = buf.length / 2
                + nextInt(buf.length / 2);
        for (int i = 0; i < curLen - 1; i++) {
            buf[i] = getRandomChar();
        }
        buf[curLen - 1] = '\n';
        return curLen;
    }
}
