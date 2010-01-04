package org.doogal.core.actor.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import net.jcip.annotations.ThreadSafe;

import org.doogal.core.actor.util.UpdateListener;

/**
 * A placeholder for some future value.
 * 
 * @author Mark Aylett
 * 
 * @param <T>
 *            the value type.
 */

@ThreadSafe
public final class FutureValue<T> implements Future<T> {

    private final class Sync extends AbstractQueuedSynchronizer implements
            Future<T> {

        private static final long serialVersionUID = 1L;

        /**
         * Future cancelled.
         */

        private static final int CANCELLED = 1;

        /**
         * Lock acquired for state transition.
         */

        private static final int ACQUIRED = 2;

        /**
         * Future completed. Resulting in either a value or an exception.
         */

        private static final int COMPLETE = 3;

        private final UpdateListener<Future<T>> listener;

        // Value and Except piggyback on State visibility.
        // This technique is known as: Piggybacking on Synchronization.
        // See JCIP, section 16.1.4.

        private T value;

        private Throwable except;

        // AbstractQueuedSynchronizer

        @Override
        protected final int tryAcquireShared(int ignore) {
            // Positive if acquisition succeeds; negative if acquisition fails.
            return isDone() ? 1 : -1;
        }

        @Override
        protected final boolean tryReleaseShared(int ignore) {
            listener.update(FutureValue.this);
            return true;
        }

        Sync(UpdateListener<Future<T>> listener) {
            this.listener = listener;
        }

        // Future

        public final boolean cancel(boolean mayInterruptIfRunning) {
            if (!compareAndSetState(0, CANCELLED))
                return false;
            releaseShared(0);
            return true;
        }

        public final boolean isCancelled() {
            return CANCELLED == getState();
        }

        public final boolean isDone() {
            final int s = getState();
            return CANCELLED == s || COMPLETE == s;
        }

        public final T get() throws InterruptedException, ExecutionException {
            acquireSharedInterruptibly(0);
            if (CANCELLED == getState())
                throw new CancellationException();
            if (null != except)
                throw new ExecutionException(except);
            return value;
        }

        public final T get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException,
                TimeoutException {
            final long nanos = unit.toNanos(timeout);
            if (!tryAcquireSharedNanos(0, nanos))
                throw new TimeoutException();
            if (CANCELLED == getState())
                throw new CancellationException();
            if (null != except)
                throw new ExecutionException(except);
            return value;
        }

        // This

        final boolean set(T value) {
            if (!compareAndSetState(0, ACQUIRED))
                return false;
            this.value = value;
            setState(COMPLETE);
            releaseShared(0);
            return true;
        }

        final boolean setException(Throwable except) {
            if (!compareAndSetState(0, ACQUIRED))
                return false;
            this.except = except;
            setState(COMPLETE);
            releaseShared(0);
            return true;
        }
    }

    private final Sync sync;

    public FutureValue(UpdateListener<Future<T>> listener) {
        sync = new Sync(listener);
    }

    // Future

    public final boolean cancel(boolean mayInterruptIfRunning) {
        return sync.cancel(mayInterruptIfRunning);
    }

    public final boolean isCancelled() {
        return sync.isCancelled();
    }

    public final boolean isDone() {
        return sync.isDone();
    }

    public final T get() throws InterruptedException, ExecutionException {
        return sync.get();
    }

    public final T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return sync.get(timeout, unit);
    }

    // This

    public final boolean set(T value) {
        return sync.set(value);
    }

    public final boolean setException(Throwable except) {
        return sync.setException(except);
    }
}
