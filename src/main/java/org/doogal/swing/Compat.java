package org.doogal.swing;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

final class Compat {
    private Compat() {

    }

    static void setRowSorter(JTable table) {
        final TableModel model = table.getModel();
        table.setRowSorter(new TableRowSorter<TableModel>(model));
    }
}
