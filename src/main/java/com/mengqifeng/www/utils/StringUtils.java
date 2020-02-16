package com.mengqifeng.www.utils;

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

    public static void main(String[] args) {
        String data = "a\00126";
        String[] words = StringUtils.split(data, '\001');
        for (String word : words) {
            System.out.println(word);
        }
        for (String word : args) {
            System.out.println(word);
        }
    }
}
