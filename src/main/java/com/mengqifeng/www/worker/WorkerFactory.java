package com.mengqifeng.www.worker;

import com.mengqifeng.www.logic.ConsoleParam;

public class WorkerFactory {

    public static IWorker createShuffleWorker(ConsoleParam param){
        return new ShuffleWorker(param);
    }
}
