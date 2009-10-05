package org.doogal.core;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;

public final class PrintView extends AbstractView {

    private final PrintPager pager;

    public PrintView(PrintWriter out, Log log) throws IOException {
        super(out, log);
        pager = new PrintPager(out);
    }

    public final void close() throws IOException {
        pager.close();
    }

    public final void setPage(String n) throws EvalException, IOException {
        pager.setPage(n);
    }

    public final void showPage() throws EvalException, IOException {
        pager.showPage();
    }

    public final void nextPage() throws EvalException, IOException {
        pager.nextPage();
    }

    public final void prevPage() throws EvalException, IOException {
        pager.prevPage();
    }

    public final void setDataSet(DataSet dataSet) throws IOException {
        pager.setDataSet(dataSet);
    }

    public DataSet getDataSet() {
        return pager.getDataSet();
    }
}
