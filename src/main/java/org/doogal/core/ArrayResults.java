package org.doogal.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;

import org.apache.lucene.index.Term;

final class ArrayResults implements Results {

    private final String[] arr;

    ArrayResults(String[] arr) {
        this.arr = arr;
    }

    public final void close() throws IOException {
    }

    public final String peek(Term term, PrintWriter out) {
        return null;
    }

    public final Collection<Term> getTerms() {
        return Collections.<Term> emptyList();
    }
    
    public final String get(int i) throws IOException {
        return arr[i];
    }

    public final int size() {
        return arr.length;
    }
}
