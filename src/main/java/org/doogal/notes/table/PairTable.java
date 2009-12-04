package org.doogal.notes.table;

import java.util.ArrayList;
import java.util.List;

public final class PairTable implements Table {
    private static class Pair {
        private final String first;
        private final String second;

        Pair(String first, String second) {
            this.first = first;
            this.second = second;
        }
    }

    private final TableType type;
    private final String firstName;
    private final String secondName;
    private final List<Pair> list;

    public PairTable(TableType type, String firstName, String secondName) {
        this.type = type;
        this.firstName = firstName;
        this.secondName = secondName;
        list = new ArrayList<Pair>();
    }

    public final void destroy() {

    }

    public final TableType getType() {
        return type;
    }

    public final int getRowCount() {
        return list.size();
    }

    public final int getColumnCount() {
        return 2;
    }

    public final String getColumnName(int columnIndex) {
        String name = null;
        switch (columnIndex) {
        case 0:
            name = firstName;
            break;
        case 1:
            name = secondName;
            break;
        }
        return name;
    }

    public final Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    public final Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        final Pair pair = list.get(rowIndex);
        switch (columnIndex) {
        case 0:
            value = pair.first;
            break;
        case 1:
            value = pair.second;
            break;
        }
        return value;
    }

    public final void add(String first, String second) {
        list.add(new Pair(first, second));
    }
}
