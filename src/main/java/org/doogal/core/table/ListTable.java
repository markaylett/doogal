package org.doogal.core.table;

import java.io.IOException;
import java.util.List;

public final class ListTable implements Table {
    private final TableType type;
    private final String columnName;
    private final List<String> list;

    public ListTable(TableType type, String columnName, List<String> list) {
        this.type = type;
        this.columnName = columnName;
        this.list = list;
    }

    public final void close() throws IOException {

    }

    public final TableType getType() {
        return type;
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
}
