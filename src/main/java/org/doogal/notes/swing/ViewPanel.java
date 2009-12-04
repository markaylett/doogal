package org.doogal.notes.swing;

import org.doogal.notes.table.TableType;
import org.doogal.notes.view.Pager;

interface ViewPanel extends Pager {

    void setVisible();

    TableType getType();

    Object[] getSelection();
}
