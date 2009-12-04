package org.doogal.notes.util;

public final class WriteOnce<T> {
    private T value;

    public WriteOnce() {
        this.value = null;
    }

    public final void set(T value) {
        if (null == this.value)
            this.value = value;
    }

    public final T get() {
        return this.value;
    }

    public final boolean isEmpty() {
        return null == value;
    }
}
