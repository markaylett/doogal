package org.doogal.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.mail.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;

public final class PrintView implements View {

    private final PrintWriter out;
    private final Log log;
    private Pager pager;

    final void setPager(Pager p) throws IOException {
        if (null != pager) {
            pager.close();
            pager = null;
        }
        pager = p;
    }
    
    public PrintView(PrintWriter out, Log log) throws IOException {
        this.out = out;
        this.log = log;
        // Avoid null pager.
        setPager(new PrintPager(ListResults.EMPTY, out));
    }

    public final void close() throws IOException {
        setPager(null);
    }
    
    public final void setPage(String n) throws EvalException, IOException {
        pager.setPage(n);
    }

    public final void showPage() throws IOException {
        pager.showPage();
    }

    public final void nextPage() throws IOException {
        pager.nextPage();
    }

    public final void prevPage() throws IOException {
        pager.prevPage();
    }
    
    public final void what(Term term) throws IOException, MessagingException {
        pager.what(term);
    }

    public final Collection<Term> terms() throws IOException {
        return pager.terms();
    }

    public final void setResults(Results results) throws IOException {
        setPager(new PrintPager(results, out));
    }

    public final Log getLog() {
        return log;
    }

    public final PrintWriter getOut() {
        return out;
    }
}
