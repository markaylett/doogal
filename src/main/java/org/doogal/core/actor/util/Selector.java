package org.doogal.core.actor.util;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.jcip.annotations.ThreadSafe;

import org.doogal.core.util.Destroyable;

/**
 * Selector that allows caller to wait for the completion of one or more
 * futures.  The futures must originate from the same source as the selector.
 * 
 * @author Mark Aylett
 * 
 * @param <T>
 *            Future value type.
 */
@ThreadSafe
public final class Selector<T> implements Destroyable,
        UpdateListener<Future<T>> {
    private final WeakReference<UpdateListenerSet<Future<T>>> reference;
    private final Monitor monitor;

    private static <T> int copyLocked(List<Future<T>> from, List<Future<T>> to) {
        int n = 0;
        final ListIterator<Future<T>> it = from.listIterator();
        while (it.hasNext()) {
            final Future<T> future = it.next();
            if (future.isDone()) {
                it.remove();
                to.add(future);
                ++n;
            }
        }
        return n;
    }

    private Selector(UpdateListenerSet<Future<T>> listenerSet) {
        this.reference = new WeakReference<UpdateListenerSet<Future<T>>>(
                listenerSet);
        this.monitor = new Monitor();
    }

    public static <T> Selector<T> newInstance(
            UpdateListenerSet<Future<T>> listenerSet) {
        final Selector<T> selector = new Selector<T>(listenerSet);
        listenerSet.addListener(selector);
        return selector;
    }

    public final void destroy() {
        final UpdateListenerSet<Future<T>> listenerSet = reference.get();
        if (null != listenerSet) {
            reference.clear();
            listenerSet.removeListener(this);
        }
    }

    public final void update(Future<T> future) {
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    public final void wait(List<Future<T>> from, List<Future<T>> to)
            throws InterruptedException {
        synchronized (monitor) {
            while (0 == copyLocked(from, to))
                monitor.wait();
        }
    }

    public final void wait(List<Future<T>> from, List<Future<T>> to,
            long timeout, TimeUnit unit) throws InterruptedException,
            TimeoutException {
        final long abs = System.currentTimeMillis() + unit.toMillis(timeout);
        synchronized (monitor) {
            while (0 == copyLocked(from, to)) {
                final long ms = abs - System.currentTimeMillis();
                if (ms <= 0)
                    throw new TimeoutException();
                monitor.wait(ms);
            }
        }
    }
}
