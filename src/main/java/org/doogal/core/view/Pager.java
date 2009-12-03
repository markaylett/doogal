package org.doogal.core.view;

import java.io.IOException;

import org.doogal.core.util.Destroyable;
import org.doogal.core.util.EvalException;

public interface Pager extends Destroyable {
    void setPage(int n) throws EvalException, IOException;

    void nextPage() throws EvalException, IOException;

    void prevPage() throws EvalException, IOException;
}
