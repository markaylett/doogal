package org.doogal.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.index.Term;

final class IdentityResults implements Results {
    private final List<Term> terms;
    private final List<String> values;

    IdentityResults() {
        terms = new ArrayList<Term>();
        values = new ArrayList<String>();
    }

    public final void close() throws IOException {
    }

    public final String get(int i) throws IOException {
        return values.get(i);
    }

    public final Collection<Term> terms() {
        return terms;
    }

    public final int size() {
        return terms.size();
    }

    void add(Term term, String value) {
        terms.add(term);
        values.add(value);
    }

    void add(String id, String value) {
        terms.add(new Term("id", id));
        values.add(value);
    }
}
