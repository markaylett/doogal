package org.doogal.core;

import static org.doogal.core.Constants.PAGE_SIZE;

import java.io.IOException;
import java.io.PrintWriter;

final class PrintPager implements Pager {
    private final PrintWriter out;
    private DataSet dataSet;
    private int start;
    private int end;

    PrintPager(PrintWriter out) throws IOException {
        this.out = out;
        setDataSet(null);
    }

    public final void close() throws IOException {
        setDataSet(null);
    }

    public final void setPage(String n) throws EvalException, IOException {
        if (null == dataSet)
            throw new EvalException("no such page");

        final int i = Math.max(Integer.valueOf(n) - 1, 0) * PAGE_SIZE;
        if (dataSet.size() <= i)
            throw new EvalException("no such page");
        start = i;
    }

    public final void showPage() throws EvalException, IOException {

        if (null == dataSet)
            throw new EvalException("no such page");

        end = Math.min(dataSet.size(), start + PAGE_SIZE);

        String prompt = null;
        if (0 < dataSet.size()) {
            final int page = 1 + start / PAGE_SIZE;
            final int total = 1 + (dataSet.size() - 1) / PAGE_SIZE;
            out.println("page " + page + " of " + total + ":");
            if (page < total)
                prompt = "more...";
        } else
            out.println("no dataSet");

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
        if (null == dataSet)
            throw new EvalException("no such page");

        if (start + PAGE_SIZE < dataSet.size())
            start += PAGE_SIZE;
    }

    public final void prevPage() throws EvalException, IOException {
        if (null == dataSet)
            throw new EvalException("no such page");

        start = Math.max(0, start - PAGE_SIZE);
    }

    public final void setDataSet(DataSet dataSet) throws IOException {
        if (null != this.dataSet)
            this.dataSet.close();
        this.dataSet = dataSet;
        start = 0;
        end = null == dataSet ? 0 : Math.min(dataSet.size(), start + PAGE_SIZE);
    }

    public final DataSet getDataSet() {
        return dataSet;
    }
}
