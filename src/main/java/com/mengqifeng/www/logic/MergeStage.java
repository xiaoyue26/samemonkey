package com.mengqifeng.www.logic;

import com.mengqifeng.www.utils.LogFactory;
import com.mengqifeng.www.utils.Logger;
import com.mengqifeng.www.utils.StringUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MergeStage implements IStage {
    private final Logger logger = LogFactory.getLogger(this.getClass());
    private final ApplicationContext context;

    public MergeStage(ApplicationContext context) {
        this.context = context;
    }
    public void run(){
        mergeAndOut();
    }
    private final int guessLineNum() {
        return (int) (context.inFile1.toFile().length()
                / 214 / context.bucketNum);
    }
    private void mergeAndOut() {
        logger.info("begin merge:");
        for (int i = 0; i < context.bucketNum; i++) {
            logger.debug("begin merge tmp_%d:", i);
            // 1. open tmp1-i build bloom+hashMap by tmp1
            // final StringBloomFilter blf = new StringBloomFilter();
            /*final BloomFilter<String> blf = BloomFilter.create(
                    Funnels.stringFunnel(StandardCharsets.UTF_8)
                    , guessLineNum());*/
            logger.debug("guessLineNum(): %d", guessLineNum());
            final Map<String, List<Long>> map = new HashMap<>();
            // final Map<String, List<Long>> map = new OpenHashMap<>();
            Path tmpPath;
            // 选择较小的来build map:
            tmpPath = Paths.get(context.tmpPath1.toString()
                    , String.valueOf(i) + context.tmpPostFix);
            try (Stream<String> lines = Files.lines(tmpPath, context.CS)) {

                lines.forEach(lineWithIndex -> {
                    String[] words = StringUtils.split(lineWithIndex, context.SEP);
                    List<Long> old = map.get(words[0]);
                    // blf.put(words[0]);// TODO remove
                    if (old == null) {
                        map.put(words[0], Arrays.asList(Long.valueOf(words[1])));
                    } else {
                        old.add(Long.valueOf(words[1]));
                    }
                    // blf.add(words[0]);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 2. delete tmp1
            tmpPath.toFile().delete();
            logger.debug("build tmp1-%d info ok", i);
            logger.debug("tmp1-%d hashmap size: %d", i, map.size());
            // 3. open tmp2-i, write out-i
            tmpPath = Paths.get(context.tmpPath2.toString()
                    , String.valueOf(i) + context.tmpPostFix);
            try (Stream<String> lines = Files.lines(tmpPath, context.CS);
                 FileWriter fw = new FileWriter(Paths.get(context.outPath.toString()
                         , String.valueOf(i) + context.tmpPostFix).toFile()
                         , true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)
            ) {
                lines.forEach(lineWithIndex -> {
                    String[] words = StringUtils.split(lineWithIndex, context.SEP);
                    // if (blf.mightContain(words[0])) {// todo remove
                    // if (blf.contains(words[0])) {// todo remove
                    List<Long> old = map.get(words[0]);
                    if (old != null) {
                        for (Long index : old) {
                            out.write(words[0] + context.SEP_STR + index + context.SEP_STR + words[1] + '\n');
                        }
                    }
                    // }
                });
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                // map = null;
                tmpPath.toFile().delete();
            }
            // 4. close file
            logger.debug("finish merge tmp_%d.", i);
        }
        // 5. merge out:
        // logger.info("begin merge res:");
        /*try (FileWriter fw = new FileWriter(Paths.get(outPath.toString()
                , resFileName).toFile(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            // mergeFiles(out);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    private void mergeFiles(PrintWriter out) {
        for (int i = 0; i < context.bucketNum; i++) {
            Path tmpPath = Paths.get(context.outPath.toString()
                    , String.valueOf(i) + context.tmpPostFix);
            try (Stream<String> lines = Files.lines(tmpPath, context.CS)) {
                lines.forEach(
                        line -> {
                            out.write(line + '\n');
                        }
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }





}
