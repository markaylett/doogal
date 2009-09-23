package org.doogal;

import java.io.IOException;
import java.io.PrintWriter;

final class ArrayResults implements Results {

    private final String[] arr;

    ArrayResults(String[] arr) {
        this.arr = arr;
    }

    public final void close() throws IOException {
    }

    public final void print(PrintWriter out, int i) throws IOException {
        final String s = arr[i];
        if (0 == s.length())
            out.println();
        else
            out.println(" " + s);
    }

    public final int size() {
        return arr.length;
    }
}
