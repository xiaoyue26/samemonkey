package com.mengqifeng.www.utils;

import java.io.File;

public class FileUtils {
    public static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public static void main(String[]args){
        // FileRegion region = new DefaultFileRegion(new File(fileName),0,1000);


    }
}
