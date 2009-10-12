package org.doogal.swing;

import java.io.IOException;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.doogal.core.table.SummaryTable;
import org.doogal.core.table.Table;
import org.doogal.core.table.TableType;

final class TableAdapter implements TableModel {
    private final Table table;
    private final EventListenerList listeners;

    TableAdapter() {
        table = new SummaryTable();
        listeners = new EventListenerList();
    }

    TableAdapter(Table table) {
        this.table = null == table ? new SummaryTable() : table;
        listeners = new EventListenerList();
    }

    public final void addTableModelListener(TableModelListener l) {
        listeners.add(TableModelListener.class, l);
    }

    public final Class<?> getColumnClass(int columnIndex) {
        return table.getColumnClass(columnIndex);
    }

    public final int getColumnCount() {
        return table.getColumnCount();
    }

    public final String getColumnName(int columnIndex) {
        return table.getColumnName(columnIndex);
    }

    public final int getRowCount() {
        return table.getRowCount();
    }

    public final Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        try {
            value = table.getValueAt(rowIndex, columnIndex);
        } catch (final IOException e) {
        }
        return value;
    }

    public final boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public final void removeTableModelListener(TableModelListener l) {
        listeners.remove(TableModelListener.class, l);
    }

    public final void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    public final TableType getType() {
        return table.getType();
    }
}
