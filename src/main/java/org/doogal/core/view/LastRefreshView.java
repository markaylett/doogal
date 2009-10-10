package org.doogal.core.view;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;
import org.doogal.core.EvalException;
import org.doogal.core.Interpreter;
import org.doogal.core.Predicate;
import org.doogal.core.Summary;
import org.doogal.core.table.Table;

public final class LastRefreshView implements RefreshView {

    private final View view;
    private final Interpreter interp;
    private boolean changed;
    private String cmd;
    private Object[] args;

    public LastRefreshView(View view, Interpreter interp) {
        this.view = view;
        this.interp = interp;
        this.changed = false;
        this.cmd = null;
        this.args = null;
    }
    
    public final void close() throws IOException {
        view.close();
    }

    public final void setTable(Table table) throws IOException {
        changed = true;
        view.setTable(table);
    }

    public final String peek(Term term) throws IOException {
        return view.peek(term);
    }

    public final void whileSummary(Predicate<Summary> pred) throws Exception {
        view.whileSummary(pred);
    }

    public final PrintWriter getOut() {
        return view.getOut();
    }

    public final Log getLog() {
        return view.getLog();
    }

    public final void setPage(String n) throws EvalException, IOException {
        view.setPage(n);
    }

    public final void showPage() throws EvalException, IOException {
        view.showPage();
    }

    public final void nextPage() throws EvalException, IOException {
        view.nextPage();
    }

    public final void prevPage() throws EvalException, IOException {
        view.prevPage();
    }

    public final void refresh() throws EvalException {
        if (null != cmd)
            interp.eval(cmd, args);
    }
    
    public final void setLast(String cmd, Object... args) {
        if (changed) {
            this.changed = false;
            this.cmd = cmd;
            this.args = args;
        }
    }
}
