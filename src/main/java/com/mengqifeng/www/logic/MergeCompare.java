package com.mengqifeng.www.logic;

import com.mengqifeng.www.utils.LogFactory;
import com.mengqifeng.www.utils.Logger;
import com.mengqifeng.www.utils.StringUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class MergeCompare implements IStage {
    private final Logger logger = LogFactory.getLogger(this.getClass());
    private final ApplicationContext context;
    private long workingProgress = 0;
    private final long logInternal = 10000 * 100;

    public MergeCompare(ApplicationContext context) {
        this.context = context;
    }

    private LineAndNum getNextValid(Iterator<String> list) {
        if (!list.hasNext()) {
            return null;
        }
        String[] word = StringUtils.leftSplit2(list.next(), context.SEP);
        workingProgress++;
        while (word.length != 2 && list.hasNext()) {
            word = StringUtils.leftSplit2(list.next(), context.SEP);
            workingProgress++;
        }
        if (word.length == 2) {
            return new LineAndNum(word[1], Long.valueOf(word[0]));
        } else {
            return null;
        }

    }

    static class LineAndNum {
        String line;
        Long num;

        LineAndNum(String li, Long n) {
            line = li;
            num = n;
        }
    }


    @Override
    public void run() throws IOException {
        try (Stream<String> stream1 = Files.lines(context.inFile1);
             Stream<String> stream2 = Files.lines(context.inFile2);
             FileWriter fw = new FileWriter(Paths.get(context.outPath.toString()
                     , 0 + context.tmpPostFix).toFile()
                     , true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)
        ) {
            Iterator<String> list1 = stream1.iterator();
            Iterator<String> list2 = stream2.iterator();
            LineAndNum head1 = getNextValid(list1);
            LineAndNum head2 = getNextValid(list2);

            while (head1 != null && head2 != null) {
                if (head1.line.compareTo(head2.line) < 0) {
                    head1 = getNextValid(list1);
                } else if (head1.line.compareTo(head2.line) > 0) {
                    head2 = getNextValid(list2);
                } else {// collect and print:
                    List<Long> numList1 = new ArrayList<>();
                    List<Long> numList2 = new ArrayList<>();
                    numList1.add(head1.num);
                    numList2.add(head2.num);
                    String line = head1.line;
                    while (head1 != null) {
                        head1 = getNextValid(list1);
                        if (head1 == null || !line.equals(head1.line)) {
                            break;
                        }
                    }
                    while (head2 != null) {
                        head2 = getNextValid(list2);
                        if (head2 == null || !line.equals(head2.line)) {
                            break;
                        }
                    }
                    for (Long n1 : numList1) {
                        for (Long n2 : numList2) {
                            out.write(line
                                    + context.SEP_STR + n1
                                    + context.SEP_STR + n2);
                        }
                    }
                }
                if (workingProgress % logInternal == 0) {
                    logger.info("has scan %d lines", workingProgress);
                }
            }
            // skip remain, do nothing

        } catch (IOException e) {
            throw e;
        }


    }
}
