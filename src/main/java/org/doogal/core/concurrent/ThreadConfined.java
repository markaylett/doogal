package org.doogal.core.concurrent;

import net.jcip.annotations.Immutable;

@Immutable
public abstract class ThreadConfined {
    private final long confined;

    protected ThreadConfined() {
        final Thread thread = Thread.currentThread();
        confined = thread.getId();
        thread.setName(getClass().getSimpleName());
    }

    protected final boolean isConfined() {
        return confined == Thread.currentThread().getId();
    }
}
