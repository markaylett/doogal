package org.doogal.core;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;

public abstract class AbstractView implements View {
    protected final PrintWriter out;
    protected final Log log;
    protected DataSet dataSet;

    protected AbstractView(PrintWriter out, Log log) {
        this.out = out;
        this.log = log;
        this.dataSet = null;
    }
    
    public void close() throws IOException {
        setDataSet(null);
    }

    public void setDataSet(DataSet dataSet) throws IOException {
        if (null != this.dataSet)
            this.dataSet.close();
        this.dataSet = dataSet;
    }
    
    public final String peek(Term term) throws IOException {
        if (null == dataSet || !(dataSet instanceof DocumentSet))
            return null;
        final DocumentSet docSet = (DocumentSet) dataSet;
        return docSet.peek(term, out);
    }

    public final void whileSummary(Predicate<Summary> pred) throws Exception {
        if (null == dataSet || !(dataSet instanceof DocumentSet))
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
