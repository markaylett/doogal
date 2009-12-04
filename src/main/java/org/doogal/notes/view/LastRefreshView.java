package org.doogal.notes.view;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;
import org.doogal.core.util.UnaryPredicate;
import org.doogal.notes.domain.Summary;
import org.doogal.notes.table.Table;
import org.doogal.notes.table.TableType;
import org.doogal.notes.util.HtmlPage;
import org.doogal.notes.util.Interpreter;

public final class LastRefreshView implements RefreshView {

    private final View view;
    private final Interpreter interp;
    private boolean changed;
    private TableType type;
    private String cmd;
    private Object[] args;

    public LastRefreshView(View view, Interpreter interp) {
        this.view = view;
        this.interp = interp;
        changed = false;
        type = null;
        cmd = null;
        args = null;
    }

    public final void destroy() {
        view.destroy();
    }

    public final void setPage(int n) {
        view.setPage(n);
    }

    public final void nextPage() {
        view.nextPage();
    }

    public final void prevPage() {
        view.prevPage();
    }

    public final void setHtml(HtmlPage html) {
        view.setHtml(html);
    }

    public final void setTable(Table table) throws IOException {
        view.setTable(table);
        changed = true;
        type = table.getType();
    }

    public final String peek(Term term) throws IOException {
        return view.peek(term);
    }

    public final void whileSummary(UnaryPredicate<Summary> pred) throws IOException {
        view.whileSummary(pred);
    }

    public final PrintWriter getOut() {
        return view.getOut();
    }

    public final Log getLog() {
        return view.getLog();
    }

    public final void refresh() throws IOException {
        view.refresh();
    }

    public final void refresh(TableType type) {
        if (type == this.type)
            interp.eval(cmd, args);
    }

    public final void setLast(String cmd, Object... args) {
        if (changed) {
            changed = false;
            this.cmd = cmd;
            this.args = args;
        }
    }

    public final TableType getType() {
        return type;
    }
}
