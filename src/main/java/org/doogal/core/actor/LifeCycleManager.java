package org.doogal.core.actor;

import static org.doogal.core.concurrent.ConcurrentUtil.launderThrowable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.doogal.core.util.Destroyable;

public final class LifeCycleManager implements Destroyable {
    private static final long TIMEOUT = 30;
    private final Set<ActorExecutor<?>> executors;

    private final void destroyFinally() {

        // May be required as a last resort if either the latch was not
        // reached or the thread was interrupted. One or more close calls may
        // have failed to reach the actor in the alloted time.

        for (final ActorExecutor<?> executor : executors) {
            final ExecutorService service = executor.getExecutorService();
            if (!service.isShutdown())
                service.shutdownNow();
        }
    }

    public LifeCycleManager() {
        executors = new HashSet<ActorExecutor<?>>();
    }

    public final void destroy() {
        final Collection<Future<Object>> futures = new ArrayList<Future<Object>>();
        final CountDownLatch latch = new CountDownLatch(executors.size());
        // Initiate close calls.
        for (final ActorExecutor<?> executor : executors)
            futures.add(executor.shutdown(latch));
        try {
            // Wait for all shutdown requests to complete.
            if (latch.await(TIMEOUT, TimeUnit.SECONDS))
                // Check for exceptions.
                for (final Future<Object> future : futures)
                    future.get();
        } catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            if (t instanceof InterruptedException)
                Thread.currentThread().interrupt();
            else
                throw launderThrowable(t);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Inlining this function causes a compiler error.
            destroyFinally();
        }
    }

    public final void init(final ObjectBroker broker)
            throws InterruptedException {
        final Collection<Future<Object>> futures = new ArrayList<Future<Object>>();
        final CountDownLatch latch = new CountDownLatch(executors.size());
        // Initiate init calls.
        for (final ActorExecutor<?> executor : executors)
            futures.add(executor.init(latch, broker));
        try {
            // Check for exceptions.
            for (final Future<Object> future : futures)
                future.get();
        } catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            throw launderThrowable(t);
        }
    }

    public final Collection<Future<Object>> refresh() {
        final Collection<Future<Object>> futures = new ArrayList<Future<Object>>();
        for (final ActorExecutor<?> executor : executors)
            futures.add(executor.refresh());
        return futures;
    }

    public final void registerExecutor(ActorExecutor<?> service) {
        executors.add(service);
    }
}