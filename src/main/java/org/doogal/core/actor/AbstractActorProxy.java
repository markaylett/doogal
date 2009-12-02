package org.doogal.core.actor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public abstract class AbstractActorProxy<T extends Actor> implements ActorProxy {
    protected final ExecutorService executor;
    protected final T actor;

    protected AbstractActorProxy(ExecutorService executor, T actor) {
        this.executor = executor;
        this.actor = actor;
    }

    public final Future<Object> refresh() {
        return executor.submit(new Callable<Object>() {
            public final Object call() {
                actor.refresh();
                return null;
            }
        });
    }
}
