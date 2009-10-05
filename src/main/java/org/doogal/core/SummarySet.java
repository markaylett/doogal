package org.doogal.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.Term;

final class SummarySet implements DocumentSet {
    private final List<Summary> summaries;

    SummarySet() {
        summaries = new ArrayList<Summary>();
    }

    public final void close() throws IOException {
    }

    public final String get(int i) throws IOException {
        return summaries.get(i).toString();
    }

    public final int size() {
        return summaries.size();
    }

    public final String peek(Term term, PrintWriter out) {
        return null;
    }

    public final Summary getSummary(int i) throws IOException {
        return summaries.get(i);
    }

    void add(Summary summary) {
        summaries.add(summary);
    }
}
