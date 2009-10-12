package org.doogal.core.view;

import org.doogal.core.EvalException;
import org.doogal.core.table.TableType;

public interface RefreshView extends View {

    void refresh(TableType type) throws EvalException;

    TableType getType();
}
