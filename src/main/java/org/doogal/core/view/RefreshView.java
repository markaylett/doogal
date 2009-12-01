package org.doogal.core.view;

import org.doogal.core.table.TableType;
import org.doogal.core.util.EvalException;

public interface RefreshView extends View {

    void refresh(TableType type) throws EvalException;

    TableType getType();
}
