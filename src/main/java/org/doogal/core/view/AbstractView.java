package org.doogal.core.view;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;
import org.doogal.core.Summary;
import org.doogal.core.table.DocumentTable;
import org.doogal.core.table.Table;
import org.doogal.core.util.Predicate;

public abstract class AbstractView implements View {
    protected final PrintWriter out;
    protected final Log log;
    protected Table table;

    protected AbstractView(PrintWriter out, Log log) {
        this.out = out;
        this.log = log;
        table = null;
    }

    public final void close() throws IOException {
        setTable(null);
    }

    public void setTable(Table table) throws IOException {
        if (null != this.table)
            this.table.close();
        this.table = table;
    }

    public final String peek(Term term) throws IOException {
        if (null == table || !(table instanceof DocumentTable))
            return null;
        final DocumentTable docTable = (DocumentTable) table;
        return docTable.peek(term, out);
    }

    public final void whileSummary(Predicate<Summary> pred) throws Exception {
        if (null == table || !(table instanceof DocumentTable))
            return;
        final DocumentTable docTable = (DocumentTable) table;
        final int n = docTable.getRowCount();
        for (int i = 0; i < n; ++i) {
            final Summary s = docTable.getSummary(i);
            if (!pred.call(s))
                break;
        }
    }

    public final Log getLog() {
        return log;
    }

    public final PrintWriter getOut() {
        return out;
    }
}
