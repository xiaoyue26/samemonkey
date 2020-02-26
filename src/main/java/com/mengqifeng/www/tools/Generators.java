package com.mengqifeng.www.tools;

import com.mengqifeng.www.utils.LogFactory;
import com.mengqifeng.www.utils.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Generators {
    private final static Logger logger = LogFactory.getLogger(Generators.class);

    public static List<Character>getCharList(Path... dictPaths){
        final List<Character> dictList = new ArrayList<>();
        for (Path dictPath : dictPaths) {
            try (Stream<String> lines = Files.lines(dictPath)) {
                lines.forEach(line -> {
                    for (char c : line.toCharArray()) {
                        if (c != '\n') {
                            dictList.add(c);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dictList;
    }

    public static ICharGenerator createChineseGen() {
        return new ChineseGenerator();
    }

    public static ICharGenerator createDictGen(List<Character> dictList) {
        // TODO 并没有考虑到字典原先的频次
        return new DictCharGenerator(dictList);
    }

    public static ICharGenerator createReorderGen(Path... dictPaths) {
        final List<Character> dictList = getCharList(dictPaths);
        logger.info("dict size: %d", dictList.size());
        return new DictCharGenerator(dictList);
    }

    public static ICharGenerator createSameRowGen(Path... dictPaths){
        final List<Character> dictList = getCharList(dictPaths);
        logger.info("dict size: %d", dictList.size());
        return new SameRowGenerator(dictList);
    }
}
