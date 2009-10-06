package org.doogal.core;

import static org.doogal.core.Constants.PAGE_SIZE;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;

public class PrintView extends AbstractView {

    private int start;
    private int end;

    public PrintView(PrintWriter out, Log log) throws IOException {
        super(out, log);
    }

    public final void setPage(String n) throws EvalException, IOException {
        final int i = Math.max(Integer.valueOf(n) - 1, 0) * PAGE_SIZE;
        if (null == dataSet || dataSet.size() <= i)
            throw new EvalException("no such page");
        start = i;
    }

    public final void showPage() throws EvalException, IOException {

        if (null == dataSet) {
            out.println("no data");
            return;
        }

        end = Math.min(dataSet.size(), start + PAGE_SIZE);

        String prompt = null;
        if (0 < dataSet.size()) {
            final int page = 1 + start / PAGE_SIZE;
            final int total = 1 + (dataSet.size() - 1) / PAGE_SIZE;
            out.println("page " + page + " of " + total + ":");
            if (page < total)
                prompt = "more...";
        } else
            out.println("no data");

        for (int i = start; i < end; i++) {

            final String s = dataSet.get(i);
            if (0 == s.length())
                out.println();
            else
                out.println(" " + s);
        }

        if (null != prompt)
            out.println(prompt);
    }

    public final void nextPage() throws EvalException, IOException {
        if (null != dataSet && start + PAGE_SIZE < dataSet.size())
            start += PAGE_SIZE;
    }

    public final void prevPage() throws EvalException, IOException {
        if (null != dataSet)
            start = Math.max(0, start - PAGE_SIZE);
    }

    public void setDataSet(DataSet dataSet) throws IOException {
        super.setDataSet(dataSet);
        start = 0;
        end = null == dataSet ? 0 : Math.min(dataSet.size(), start + PAGE_SIZE);
    }
}
