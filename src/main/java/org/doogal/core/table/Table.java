package org.doogal.core.table;

public interface Table {
    int getRowCount();

    int getColumnCount();

    String getColumnName(int columnIndex);

    Class<?> getColumnClass(int columnIndex);

    Object getValueAt(int rowIndex, int columnIndex);
}
