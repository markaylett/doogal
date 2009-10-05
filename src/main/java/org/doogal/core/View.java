package org.doogal.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;

public interface View extends Closeable {

    void setDataSet(DataSet dataSet) throws IOException;

    String peek(Term term) throws IOException;

    void whileSummary(Predicate<Summary> pred) throws Exception;

    PrintWriter getOut();

    Log getLog();

    void setPage(String n) throws EvalException, IOException;

    void showPage() throws EvalException, IOException;

    void nextPage() throws EvalException, IOException;

    void prevPage() throws EvalException, IOException;
}
