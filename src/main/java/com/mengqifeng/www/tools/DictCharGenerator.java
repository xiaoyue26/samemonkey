package com.mengqifeng.www.tools;

import java.util.List;
import java.util.Random;

public class DictCharGenerator implements ICharGenerator {
    private final Random random;
    private final List<Character> dictList;

    DictCharGenerator(List<Character> dictList) {
        this.dictList = dictList;
        random = new Random(System.currentTimeMillis());
    }

    public final char getRandomChar() {
        return dictList.get(random.nextInt(dictList.size()));
    }

    @Override
    public int nextInt(int bound) {
        return random.nextInt(bound);
    }

}
