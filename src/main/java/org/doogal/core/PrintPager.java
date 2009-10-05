package org.doogal.core;

import static org.doogal.core.Constants.PAGE_SIZE;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;

import javax.mail.MessagingException;

import org.apache.lucene.index.Term;

final class PrintPager implements Pager {
    private final DataSet dataSet;
    private final PrintWriter out;
    private int start;
    private int end;

    PrintPager(DataSet dataSet, PrintWriter out) throws IOException {
        this.dataSet = dataSet;
        this.out = out;
        start = 0;
        end = Math.min(dataSet.size(), start + PAGE_SIZE);
    }

    public final void close() throws IOException {
        dataSet.close();
    }
    
    public final void setPage(String n) throws EvalException, IOException {
        final int i = Math.max(Integer.valueOf(n) - 1, 0) * PAGE_SIZE;
        if (dataSet.size() <= i)
            throw new EvalException("no such page");
        start = i;
    }

    public final void showPage() throws IOException {

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

    public final void nextPage() throws IOException {
        if (start + PAGE_SIZE < dataSet.size())
            start += PAGE_SIZE;
    }

    public final void prevPage() throws IOException {
        start = Math.max(0, start - PAGE_SIZE);
    }

    public final String peek(Term term) throws IOException, MessagingException {
        if (!(dataSet instanceof DocumentSet))
            return null;
        final DocumentSet docSet = (DocumentSet) dataSet;
        return docSet.peek(term, out);
    }

    public final Collection<Term> terms() throws IOException {
        if (!(dataSet instanceof DocumentSet))
            return Collections.<Term>emptyList();
        final DocumentSet docSet = (DocumentSet) dataSet;
        return docSet.getTerms();
    }
}
