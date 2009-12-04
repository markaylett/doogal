package org.doogal.notes.table;

public final class ArrayTable implements Table {
    private final TableType type;
    private final String columnName;
    private final String[] list;

    public ArrayTable(TableType type, String columnName, String[] list) {
        this.type = type;
        this.columnName = columnName;
        this.list = list;
    }

    public final void destroy() {

    }

    public final TableType getType() {
        return type;
    }

    public final int getRowCount() {
        return list.length;
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
        return list[rowIndex];
    }
}
