package com.mengqifeng.www.logic;

import com.mengqifeng.www.utils.MmapInStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class InputStreams {

    public static InputStream newInStream(Path path, boolean useMmap) throws IOException {
        if (useMmap) {
            return new MmapInStream(path.toFile());
        } else {
            return Files.newInputStream(path);
        }
    }
}
