package org.doogal.core.view;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;
import org.doogal.core.EvalException;
import org.doogal.core.Summary;
import org.doogal.core.table.Table;
import org.doogal.core.util.Html;
import org.doogal.core.util.Predicate;

public interface View extends Closeable {

    void setTable(Table table) throws IOException;

    String peek(Term term) throws IOException;

    void whileSummary(Predicate<Summary> pred) throws Exception;

    PrintWriter getOut();

    Log getLog();

    void setHtml(Html html);

    void setPage(int n) throws EvalException, IOException;

    void showPage() throws EvalException, IOException;

    void nextPage() throws EvalException, IOException;

    void prevPage() throws EvalException, IOException;
}
