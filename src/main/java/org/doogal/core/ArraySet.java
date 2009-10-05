package org.doogal.core;

import java.io.IOException;

final class ArraySet implements DataSet {

    private final String[] arr;

    ArraySet(String[] arr) {
        this.arr = arr;
    }

    public final void close() throws IOException {
    }
    
    public final String get(int i) throws IOException {
        return arr[i];
    }

    public final int size() {
        return arr.length;
    }
}
