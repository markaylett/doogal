package org.doogal.core.table;

import java.util.Date;
import org.doogal.core.Summary;

abstract class AbstractTable implements Table {
    protected static Object getValueAt(Summary summary, int columnIndex) {
        Object value = null;
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
        }
        return value;
    }
    
    public final int getColumnCount() {
        return 3;
    }

    public final String getColumnName(int columnIndex) {
        String name = null;
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
            clazz = Date.class;
            break;
        case 2:
            clazz = String.class;
            break;
        }
        return clazz;
    }
}
