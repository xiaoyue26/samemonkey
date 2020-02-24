package com.mengqifeng.www.logic;

import com.mengqifeng.www.utils.HashUtils;

public class Node {

    public final byte[] data;

    public Node(byte[] buf, int left, int right) {
        data = new byte[right - left + 1];
        // src,srcPos,dst,dstPos,len
        System.arraycopy(buf, left, data, 0, data.length);
    }

    @Override
    public int hashCode() {
        return HashUtils.hashCode(data, 0, data.length - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Node)) return false;
        final Node other = (Node) o;
        if (data.length != other.data.length) {
            return false;
        }
        for (int i = 0; i < data.length; i++) {
            if (other.data[i] != this.data[i]) {
                return false;
            }
        }
        return true;
    }
}
