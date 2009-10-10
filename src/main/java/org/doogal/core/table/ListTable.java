package org.doogal.core.table;

import java.io.IOException;
import java.util.List;

public final class ListTable implements Table {
    private final String columnName;
    private final String action;
    private final String[] actions;
    private final List<String> list;

    public ListTable(String columnName, String action, String[] actions, List<String> list) {
        this.columnName = columnName;
        this.action = action;
        this.actions = actions;
        this.list = list;
    }

    public void close() throws IOException {

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

    public final String getAction() {
        return action;
    }
    
    public final String[] getActions() {
        return actions;
    }
}
