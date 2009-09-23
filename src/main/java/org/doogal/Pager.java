package org.doogal;

import static org.doogal.Constants.PAGE_SIZE;

import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.index.Term;

final class Pager {
    private final Results results;
    private int start;
    private int end;

    Pager(Results results) throws IOException {
        this.results = results;
        start = 0;
        end = Math.min(results.size(), start + PAGE_SIZE);
    }

    final void close() throws IOException {
        results.close();
    }

    final Collection<Term> terms() throws IOException {
        return results.terms();
    }

    final void execGoto(String n) throws IOException {
        final int i = Math.max(Integer.valueOf(n) - 1, 0);
        if (i * PAGE_SIZE < results.size())
            start = i * PAGE_SIZE;
        else
            System.err.println("no such page");
    }

    final void execList() throws IOException {

        end = Math.min(results.size(), start + PAGE_SIZE);

        String prompt = null;
        if (0 < results.size()) {
            final int page = 1 + start / PAGE_SIZE;
            final int total = 1 + (results.size() - 1) / PAGE_SIZE;
            System.out.println("page " + page + " of " + total + ":");
            if (page < total)
                prompt = "more...";
        } else
            System.out.println("no results");

        for (int i = start; i < end; i++) {

            final String s = results.get(i);
            if (0 == s.length())
                System.out.println();
            else
                System.out.println(" " + s);
        }

        if (null != prompt)
            System.out.println(prompt);
    }

    final void execNext() throws IOException {
        if (start + PAGE_SIZE < results.size())
            start += PAGE_SIZE;
    }

    final void execPrev() throws IOException {
        start = Math.max(0, start - PAGE_SIZE);
    }
}
