package com.mengqifeng.www.logic;

import java.io.IOException;

public class SortSuffleStage implements IShuffleStage {

    @Override
    public void shuffle(int i) throws IOException {
        // 为节省内存、避免重复造轮子，由run.sh脚本中的sort命令代劳
    }
}
