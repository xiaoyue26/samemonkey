package com.mengqifeng.www.logic;

import java.io.IOException;

public interface IShuffleStage {
    void shuffle(int i) throws IOException;
    default void run() throws IOException{
        // 1. 读取第1个文件=>写到tmp/{epoch}/1/n个文件;
        shuffle(0);
        // 2. 读取第2个文件=>写到tmp/{epoch}/2/n个文件;
        shuffle(1);
    }
}
