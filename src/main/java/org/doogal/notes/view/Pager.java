package org.doogal.notes.view;

import org.doogal.core.util.Destroyable;

public interface Pager extends Destroyable {
    void setPage(int n);

    void nextPage();

    void prevPage();
}
