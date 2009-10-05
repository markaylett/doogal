package org.doogal.swing;

import java.util.ArrayList;
import java.util.List;

final class History {
    private static final int MAX = 100;
    private final List<String> history;
    private int index;

    private final String get() {
        return -1 == index ? "" : history.get(index);
    }

    History() {
        history = new ArrayList<String>();
        index = -1;
    }

    final void add(String s) {
        if (history.isEmpty() || !history.get(0).equals(s)) {
            history.add(0, s);
            if (MAX < history.size())
                history.remove(MAX);
        }
        index = -1;
    }

    final String next() {
        if (0 <= index)
            --index;
        return get();
    }

    final String prev() {
        if (index < history.size() - 1)
            ++index;
        return get();
    }
}
