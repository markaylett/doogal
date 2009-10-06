package org.doogal.core.view;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;
import org.doogal.core.DataSet;
import org.doogal.core.EvalException;
import org.doogal.core.Predicate;
import org.doogal.core.Summary;

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
