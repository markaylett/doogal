package org.doogal.core.actor.util;

import java.util.List;

public final class ListReference<T> {
    private final List<T> list;
    private final int index;

    public ListReference(List<T> list, int index) {
        this.list = list;
        this.index = index;
    }

    public final void set(T value) {
        list.set(index, value);
    }

    public final T get() {
        return list.get(index);
    }
}
