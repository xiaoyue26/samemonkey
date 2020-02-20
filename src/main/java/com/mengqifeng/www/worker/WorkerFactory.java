package com.mengqifeng.www.worker;

import com.mengqifeng.www.logic.ConsoleParam;

public class WorkerFactory {

    public static IWorker createWorker(ConsoleParam param){
        return new HashMergeWork(param);
    }
}
