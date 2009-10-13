package org.doogal.core.util;

public final class Holder<T> {
    private T value;
    public Holder() {
        this.value = null;
    }
    public Holder(T value) {
        this.value = value;
    }
    public final void set(T value) {
        this.value = value;
    }
    public final T get() {
        return this.value;
    }
    public final boolean isEmpty() {
        return null == value;
    }
}
