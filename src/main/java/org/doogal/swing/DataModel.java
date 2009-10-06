package org.doogal.swing;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

final class DataModel implements TableModel {
    private final String[] dataSet;
    private final EventListenerList listeners;

    DataModel(String[] dataSet) {
        this.dataSet = dataSet;
        this.listeners = new EventListenerList();
    }

    public final void addTableModelListener(TableModelListener l) {
        listeners.add(TableModelListener.class, l);
    }

    public final Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public final int getColumnCount() {
        return 1;
    }

    public final String getColumnName(int columnIndex) {
        return "Value";
    }

    public final int getRowCount() {
        return dataSet.length;
    }

    public final Object getValueAt(int rowIndex, int columnIndex) {
        return dataSet[rowIndex];
    }

    public final boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public final void removeTableModelListener(TableModelListener l) {
        listeners.remove(TableModelListener.class, l);
    }

    public final void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
}
