package org.doogal.swing;

import java.util.Date;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.doogal.core.Summary;

final class DocumentModel implements TableModel {
    private final Summary[] docSet;
    private final EventListenerList listeners;

    DocumentModel(Summary[] docSet) {
        this.docSet = docSet;
        this.listeners = new EventListenerList();
    }

    public final void addTableModelListener(TableModelListener l) {
        listeners.add(TableModelListener.class, l);
    }

    public final Class<?> getColumnClass(int columnIndex) {
        Class<?> clazz;
        switch (columnIndex) {
        case 0:
            clazz = Integer.class;
            break;
        case 1:
            clazz = Date.class;
            break;
        case 2:
            clazz = String.class;
            break;
        default:
            clazz = Object.class;
        }
        return clazz;
    }

    public final int getColumnCount() {
        return 3;
    }

    public final String getColumnName(int columnIndex) {
        String name;
        switch (columnIndex) {
        case 0:
            name = "Id";
            break;
        case 1:
            name = "Modified";
            break;
        case 2:
            name = "Display";
            break;
        default:
            name = "Unknown";
        }
        return name;
    }

    public final int getRowCount() {
        return docSet.length;
    }

    public final Object getValueAt(int rowIndex, int columnIndex) {
        Object value;
        final Summary summary = docSet[rowIndex];
        switch (columnIndex) {
        case 0:
            value = Integer.valueOf(summary.getId());
            break;
        case 1:
            value = summary.getModified();
            break;
        case 2:
            value = summary.getDisplay();
            break;
        default:
            value = "Unknown";
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
}
