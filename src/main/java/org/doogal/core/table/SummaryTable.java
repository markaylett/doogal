package org.doogal.core.table;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.Term;
import org.doogal.core.domain.Summary;

public final class SummaryTable extends AbstractTable implements DocumentTable {
    private final List<Summary> list;

    public SummaryTable() {
        list = new ArrayList<Summary>();
    }

    public void destroy() {

    }

    public final int getRowCount() {
        return list.size();
    }

    public final Object getValueAt(int rowIndex, int columnIndex) {
        final Summary summary = list.get(rowIndex);
        return getValueAt(summary, columnIndex);
    }

    public final String peek(Term term, PrintWriter out) throws IOException {
        return null;
    }

    public final Summary getSummary(int i) throws IOException {
        return list.get(i);
    }

    public final void add(Summary summary) {
        list.add(summary);
    }
}
