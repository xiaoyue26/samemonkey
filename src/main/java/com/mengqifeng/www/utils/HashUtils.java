package com.mengqifeng.www.utils;

public class HashUtils {

    public static int hashCode(byte a[], int left,int right) {
        if (a == null)
            return 0;

        int result = 1;
        for (int i = left; i <= right; i++) {
            result = 31 * result + a[i];
        }
        return result;
    }
}
