package com.mengqifeng.www.logic;

import java.io.IOException;

public interface IMergeStage {
    void mergeAndOut() throws IOException;

    default void run() throws IOException {
        mergeAndOut();
    }
}
