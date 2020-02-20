package com.mengqifeng.www.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 存储结构不够紧凑，用位图应该会更快
 * Created by MatveyI on 22.02.2015.
 * <p>
 * Linear probing hash map implementation
 */
public class OpenHashMap<K, V> implements Map<K, V> {

    private static final int DEAFAULT_CAPACITY = 16;
    private int size = 0;
    private int threshold;

    private Entry<K, V>[] table;

    public OpenHashMap() {
        table = new Entry[DEAFAULT_CAPACITY];
        threshold = DEAFAULT_CAPACITY - 1;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * <p>
     * If there already is an item under the given key, it will be replaced by the new value. <p>
     *
     * @param key   may not be null
     * @param value may be null, in which case the cache entry will be removed (if it existed).
     * @return the previous value, or null if none
     */
    @Override
    public V put(K key, V value) {

        checkKey(key);

        if (size >= threshold)
            resize();

        int i = getIndexFor(key);
        Entry<K, V> entry;

        do {
            entry = table[i];

            if (entry == null) {
                table[i] = new Entry<K, V>(key, value);
                size++;
                return null;
            }

            if (key.equals(entry.getKey())) {
                V oldValue = entry.getValue();
                entry.setValue(value);
                return oldValue;
            }

            i = getNextIndex(i);

        } while (true);
    }

    /**
     * Get the mapping for this key from this map if present. Null key is not allowed.
     *
     * @return the value for the given key. Null if there is no value.
     */
    @Override
    public V get(Object key) {

        checkKey(key);

        int i = getIndexFor(key);
        Entry<K, V> entry;

        do {
            entry = table[i];

            if (entry == null)
                return null;

            if (key.equals(entry.getKey())) {
                return entry.getValue();
            }

            i = getNextIndex(i);

        } while (true);
    }

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param key key whose mapping is to be removed from the map
     * @return the previous value associated with key, or null
     */
    public V remove(Object key) {

        checkKey(key);

        int i = getIndexFor(key);

        while (true) {
            Entry<K, V> item = table[i];

            if (item == null)
                return null;

            if (key.equals(item.getKey())) {
                size--;
                table[i] = null;
                closeDeletion(i);
                return item.getValue();
            }

            i = getNextIndex(i);
        }

    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("reject put all");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("reject clear");
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("reject keySet");
    }

    @Override
    public Collection<V> values() {
        throw new UnsupportedOperationException("reject values");
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException("reject entrySet");
    }

    private void closeDeletion(int indexToDelete) {

        Entry<K, V> entry;
        int i = getNextIndex(indexToDelete);

        while ((entry = table[i]) != null) {

            int r = getIndexFor(entry.getKey());
            if ((i < r && (r <= indexToDelete || indexToDelete <= i)) || (r <= indexToDelete && indexToDelete <= i)) {
                table[indexToDelete] = entry;
                table[i] = null;
                indexToDelete = i;
            }

            i = getNextIndex(i);
        }
    }


    /**
     * How many entries this map contains.
     */
    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException("reject containsKey");
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("reject containsValue");
    }

    private void resize() {
        int newCapacity = table.length << 1, i;
        threshold = newCapacity - 1;

        if (newCapacity < 0) {
            newCapacity = Integer.MAX_VALUE;
            threshold = Integer.MAX_VALUE;
        }

        Entry<K, V>[] newTable = new Entry[newCapacity];


        for (Entry<K, V> entry : table) {

            if (entry == null)
                continue;

            i = getIndexFor(entry.getKey());

            do {
                if (newTable[i] == null) {
                    newTable[i] = entry;
                    break;
                }

                i = getNextIndex(i);
            } while (true);
        }

        table = newTable;
    }

    private void checkKey(Object key) {
        if (key == null) throw new IllegalArgumentException("Key should be not null");
    }

    private int getIndexFor(Object key) {
        return key.hashCode() & threshold;
    }

    private int getNextIndex(int i) {
        return i + 1 & threshold;
    }

    @Override
    public String toString() {
        return "OpenHashMap{" +
                "size=" + size +
                ", table=" + Arrays.toString(table) +
                '}';
    }

    public static class Entry<K, V> {
        private final K key;
        private V value;
        private int hash;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;

            Entry<K, V> entry = (Entry<K, V>) o;

            if (!key.equals(entry.key)) return false;
            if (!value.equals(entry.value)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            if (hash == 0) {
                hash = 31 * key.hashCode() + value.hashCode();
            }
            return hash;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "key=" + key +
                    ", value=" + value +
                    '}';
        }

    }
}
