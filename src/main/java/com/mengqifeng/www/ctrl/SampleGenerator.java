package com.mengqifeng.www.ctrl;

import com.mengqifeng.www.tools.Generators;
import com.mengqifeng.www.tools.ICharGenerator;
import com.mengqifeng.www.utils.LogFactory;
import com.mengqifeng.www.utils.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class SampleGenerator {
    private final Logger logger = LogFactory.getLogger(this.getClass());

    public void genSampleLikeDict(Path targetPath
            , long maxLines, final int maxLineLen
            , Path... dictPaths) {
        ICharGenerator gener = Generators.createReorderGen(dictPaths);
        genSample(targetPath, maxLines, maxLineLen, gener);
    }

    @SuppressWarnings("Duplicates")
    public void genSampleFromDict(Path targetPath
            , long maxLines, final int maxLineLen
            , Path... dictPaths) {
        final Set<Character> dict = new HashSet<>();
        for (Path dictPath : dictPaths) {
            try (Stream<String> lines = Files.lines(dictPath)) {
                lines.forEach(line -> {
                    for (char c : line.toCharArray()) {
                        if (c != '\n' && c != '\001') {
                            dict.add(c);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.info("dict size: %d", dict.size());
        List<Character> dictList = new ArrayList<>(dict.size());
        dictList.addAll(dict);
        // genSample from dictList:
        ICharGenerator gener = Generators.createDictGen(dictList);
        genSample(targetPath, maxLines, maxLineLen, gener);
    }


    public void genSample(Path targetPath
            , long maxLines, final int maxLineLen) {
        ICharGenerator gener = Generators.createChineseGen();
        genSample(targetPath, maxLines, maxLineLen, gener);
    }

    private void genSample(Path targetPath
            , long maxLines, final int maxLineLen
            , ICharGenerator gener) {
        try (FileWriter fw = new FileWriter(targetPath.toFile()
                , true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            long curLines = 0;

            char[] buf = new char[maxLineLen];
            int curLen;

            while (curLines < maxLines) {
                curLen = gener.fillRandomLine(buf);
                out.write(buf, 0, curLen);
                curLines++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SampleGenerator sgen = new SampleGenerator();

        final int maxLineLen = 101;
        /*sgen.genSample(Paths.get("D:/work/old/sample1.txt")
                , maxLines, maxLineLen);
        sgen.genSample(Paths.get("D:/work/old/sample2.txt")
                , maxLines, maxLineLen);*/
        Path target = Paths.get(args[0]); // 结果路径
        long maxLines = Long.valueOf(args[1]);//50 * 1000 * 1000;
        Path dictPath1 = Paths.get(args[2]);// 字典1的路径
        sgen.genSampleLikeDict(target
                , maxLines, maxLineLen
                , dictPath1);
    }
}
