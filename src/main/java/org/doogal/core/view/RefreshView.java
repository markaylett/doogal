package org.doogal.core.view;

import org.doogal.core.EvalException;

public interface RefreshView extends View {

    void refresh() throws EvalException;
}
