package com.mengqifeng.www.utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtils {
    public static String[] split(String data, char sep) {
        CharSequence[] temp = new CharSequence[(data.length() / 2) + 1];
        int wordCount = 0;
        int i = 0;
        int j = data.indexOf(sep, 0); // first substring

        while (j >= 0) {
            temp[wordCount++] = data.substring(i, j);
            i = j + 1;
            j = data.indexOf(sep, i); // rest of substrings
        }
        temp[wordCount++] = data.substring(i); // last substring
        String[] result = new String[wordCount];
        System.arraycopy(temp, 0, result, 0, wordCount);
        return result;
    }

    public static String[] rightSplit2(String data, char sep) {
        int l = data.lastIndexOf(sep);
        if (l >= 0) {
            String[] result = new String[2];
            result[0] = data.substring(0, l);
            result[1] = data.substring(l + 1);
            return result;
        } else {
            return new String[]{data};
        }
    }

    public static String[] leftSplit2(String data, char sep) {
        int r = data.indexOf(sep);
        if (r >= 0) {
            return new String[]{data.substring(0, r)
                    , data.substring(r + 1)};
        } else {
            return new String[]{data};
        }
    }

    public static void main(String[] args) {
        String data = "a\001b\001c";
        String[] words = StringUtils.rightSplit2(data, '\001');
        for (String word : words) {
            System.out.println(word);
        }
        for (String word : args) {
            System.out.println(word);
        }
        char ss = '着';
        byte b = (byte) ss;
        System.out.println(b);
        System.out.println((int) ss);
    }
}
