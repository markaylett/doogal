package org.doogal.core.view;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;
import org.doogal.core.domain.Summary;
import org.doogal.core.table.Table;
import org.doogal.core.util.EvalException;
import org.doogal.core.util.HtmlPage;
import org.doogal.core.util.Predicate;

public interface View extends Pager {

    void setHtml(HtmlPage html);

    void setTable(Table table) throws IOException;

    String peek(Term term) throws IOException;

    void whileSummary(Predicate<Summary> pred) throws Exception;

    PrintWriter getOut();

    Log getLog();

    void refresh() throws EvalException, IOException;
}
