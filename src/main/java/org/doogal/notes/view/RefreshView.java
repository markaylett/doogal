package org.doogal.notes.view;

import org.doogal.notes.table.TableType;
import org.doogal.notes.util.EvalException;

public interface RefreshView extends View {

    void refresh(TableType type);

    TableType getType();
}
