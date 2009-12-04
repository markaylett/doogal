package org.doogal.notes.table;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.lucene.index.Term;
import org.doogal.notes.domain.Summary;

public interface DocumentTable extends Table {

    String peek(Term term, PrintWriter out) throws IOException;

    Summary getSummary(int i) throws IOException;
}
