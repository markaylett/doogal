package org.doogal.core.table;

import java.util.ArrayList;
import java.util.List;

import org.doogal.core.Summary;

final class SummaryTable extends AbstractTable {
    private final List<Summary> list;

    SummaryTable() {
        this.list = new ArrayList<Summary>();
    }

    public final int getRowCount() {
        return list.size();
    }

    public final Object getValueAt(int rowIndex, int columnIndex) {
        final Summary summary = list.get(rowIndex);
        return getValueAt(summary, columnIndex);
    }

    final void add(Summary summary) {
        list.add(summary);
    }
}
