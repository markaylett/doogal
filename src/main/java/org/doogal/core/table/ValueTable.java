package org.doogal.core.table;

import java.util.ArrayList;
import java.util.List;

final class ValueTable implements Table {
    private final String columnName;
    private final List<String> list;
    ValueTable(String columnName) {
        this.columnName = columnName;
        this.list = new ArrayList<String>();
    }
    public final int getRowCount() {
        return list.size();
    }

    public final int getColumnCount() {
        return 1;
    }

    public final String getColumnName(int columnIndex) {
        return columnName;
    }

    public final Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public final Object getValueAt(int rowIndex, int columnIndex) {
        return list.get(rowIndex);
    }
    final void add(String value) {
        list.add(value);
    }
}
