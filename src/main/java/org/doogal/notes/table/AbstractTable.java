package org.doogal.notes.table;

import java.util.Date;

import org.doogal.notes.domain.Summary;
import org.doogal.notes.util.Size;

public abstract class AbstractTable implements Table {
    protected static Object getValueAt(Summary summary, int columnIndex) {
        Object value = null;
        switch (columnIndex) {
        case 0:
            value = Integer.valueOf(summary.getId());
            break;
        case 1:
            value = new Size(summary.getSize());
            break;
        case 2:
            value = summary.getModified();
            break;
        case 3:
            value = summary.getDisplay();
            break;
        }
        return value;
    }

    public final TableType getType() {
        return TableType.DOCUMENT;
    }

    public final int getColumnCount() {
        return 4;
    }

    public final String getColumnName(int columnIndex) {
        String name = null;
        switch (columnIndex) {
        case 0:
            name = "id";
            break;
        case 1:
            name = "size";
            break;
        case 2:
            name = "modified";
            break;
        case 3:
            name = "display";
            break;
        }
        return name;
    }

    public final Class<?> getColumnClass(int columnIndex) {
        Class<?> clazz = null;
        switch (columnIndex) {
        case 0:
            clazz = Integer.class;
            break;
        case 1:
            clazz = Size.class;
            break;
        case 2:
            clazz = Date.class;
            break;
        case 3:
            clazz = String.class;
            break;
        }
        return clazz;
    }
}
