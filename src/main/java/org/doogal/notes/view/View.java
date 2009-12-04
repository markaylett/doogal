package org.doogal.notes.view;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;
import org.doogal.core.util.UnaryPredicate;
import org.doogal.notes.domain.Summary;
import org.doogal.notes.table.Table;
import org.doogal.notes.util.HtmlPage;

public interface View extends Pager {

    void setHtml(HtmlPage html);

    void setTable(Table table) throws IOException;

    String peek(Term term) throws IOException;

    void whileSummary(UnaryPredicate<Summary> pred) throws IOException;

    PrintWriter getOut();

    Log getLog();

    void refresh() throws IOException;
}
