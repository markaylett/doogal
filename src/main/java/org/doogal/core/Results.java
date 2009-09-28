package org.doogal.core;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.index.Term;

interface Results {
    void close() throws IOException;

    String get(int i) throws IOException;

    Collection<Term> terms() throws IOException;

    int size();
}
