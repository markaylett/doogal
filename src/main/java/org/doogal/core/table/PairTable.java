package org.doogal.core.table;

import java.io.IOException;
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

    private final String firstName;
    private final String secondName;
    private final String action;
    private final String[] actions;
    private final List<Pair> list;

    public PairTable(String firstName, String secondName, String action, String[] actions) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.action = action;
        this.actions = actions;
        list = new ArrayList<Pair>();
    }

    public void close() throws IOException {

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
    public final String getAction() {
        return action;
    }
    public final String[] getActions() {
        return actions;
    }
    public final void add(String first, String second) {
        list.add(new Pair(first, second));
    }
}
