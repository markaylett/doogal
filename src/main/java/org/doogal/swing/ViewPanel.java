package org.doogal.swing;

import org.doogal.core.table.TableType;
import org.doogal.core.view.Pager;

interface ViewPanel extends Pager {

    void setVisible();

    TableType getType();
    
    Object[] getSelection();
}
