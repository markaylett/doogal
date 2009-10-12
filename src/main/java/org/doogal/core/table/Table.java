package org.doogal.core.table;

import java.io.Closeable;
import java.io.IOException;

public interface Table extends Closeable {
    TableType getType();

    int getRowCount();

    int getColumnCount();

    String getColumnName(int columnIndex);

    Class<?> getColumnClass(int columnIndex);

    Object getValueAt(int rowIndex, int columnIndex) throws IOException;
}
