package org.doogal.core;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;

abstract class AbstractView implements View {
    private final PrintWriter out;
    private final Log log;

    AbstractView(PrintWriter out, Log log) {
        this.out = out;
        this.log = log;
    }

    public final String peek(Term term) throws IOException {
        final DataSet dataSet = getDataSet();
        if (!(dataSet instanceof DocumentSet))
            return null;
        final DocumentSet docSet = (DocumentSet) dataSet;
        return docSet.peek(term, out);
    }

    public final void whileSummary(Predicate<Summary> pred) throws Exception {
        final DataSet dataSet = getDataSet();
        if (!(dataSet instanceof DocumentSet))
            return;
        final DocumentSet docSet = (DocumentSet) dataSet;
        final int n = docSet.size();
        for (int i = 0; i < n; ++i) {
            final Summary s = docSet.getSummary(i);
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
