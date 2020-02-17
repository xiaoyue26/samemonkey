package com.mengqifeng.www.utils;

public class OpenHashSet {
    private final int initialCapacity;
    private final double loadFactor;
    private final int capacity;

    public OpenHashSet(int initialCapacity) {
        this.initialCapacity = initialCapacity;
        this.loadFactor = 0.7;
        capacity= initialCapacity;
    }

    public static void main(String[] args) {

    }
}
