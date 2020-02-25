package com.mengqifeng.www.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

public class FutureUtils {

    public static void submitAndCheck(int start, int endEx
            , final Function<Integer, Exception> fun)
            throws IOException {
        List<Future<Exception>> futures = submitRange(start, endEx, fun);
        checkSuccess(futures);
    }

    public static void checkSuccess(List<Future<Exception>> futures)
            throws IOException {
        for (Future<Exception> f : futures) {
            try {
                Exception e = f.get();
                if (e != null) {
                    if (e instanceof IOException) {
                        throw (IOException) e;
                    } else {
                        e.printStackTrace();
                        throw new RuntimeException("concurrent failed");
                    }

                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                throw new RuntimeException("concurrent failed");
            }
        }
    }

    public static List<Future<Exception>> submitRange(int start, int endEx
            , final Function<Integer, Exception> fun) {
        List<Integer> num = new ArrayList<>();
        for (int i = start; i < endEx; i++) {
            num.add(i);
        }
        return num.stream()
                .map(i -> CompletableFuture.supplyAsync(
                        () -> fun.apply(i))
                ).collect(Collectors.toList());
    }

    public static List<Future<Boolean>> submitRange(int start, int endEx
            , final IntPredicate fun) {
        List<Integer> num = new ArrayList<>();
        for (int i = start; i < endEx; i++) {
            num.add(i);
        }
        return num.stream()
                .map(i -> CompletableFuture.supplyAsync(
                        () -> fun.test(i))
                ).collect(Collectors.toList());
    }
}
