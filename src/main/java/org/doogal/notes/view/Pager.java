package org.doogal.notes.view;

import java.io.IOException;

import org.doogal.core.util.Destroyable;
import org.doogal.notes.util.EvalException;

public interface Pager extends Destroyable {
    void setPage(int n);

    void nextPage();

    void prevPage();
}
