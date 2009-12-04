package org.doogal.notes.table;

import java.io.IOException;

import org.doogal.core.util.Destroyable;

public interface Table extends Destroyable {
    TableType getType();

    int getRowCount();

    int getColumnCount();

    String getColumnName(int columnIndex);

    Class<?> getColumnClass(int columnIndex);

    Object getValueAt(int rowIndex, int columnIndex) throws IOException;
}
