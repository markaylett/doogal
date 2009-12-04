package org.doogal.notes.view;

import org.doogal.notes.table.TableType;

public interface RefreshView extends View {

    void refresh(TableType type);

    TableType getType();
}
