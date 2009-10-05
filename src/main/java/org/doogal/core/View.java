package org.doogal.core;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;

public interface View extends Pager {
    String peek(Term term) throws IOException;

    void whileSummary(Predicate<Summary> pred) throws Exception;

    PrintWriter getOut();

    Log getLog();
}
