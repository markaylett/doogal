package org.doogal.core.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.jcip.annotations.ThreadSafe;

import org.doogal.core.util.Destroyable;

/**
 * ScheduledExecutorService that implements the {@link Destroyable} interface.
 * 
 * @author Mark Aylett
 */

@ThreadSafe
public final class ScheduledExecutor implements Destroyable,
        ScheduledExecutorService {
    private final ScheduledExecutorService scheduler;

    public ScheduledExecutor() {
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public final void destroy() {
        ConcurrentUtil.destroy(scheduler);
    }

    public final boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        return scheduler.awaitTermination(timeout, unit);
    }

    public final void execute(Runnable command) {
        scheduler.execute(command);
    }

    public final <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return scheduler.invokeAll(tasks, timeout, unit);
    }

    public final <T> List<Future<T>> invokeAll(
            Collection<? extends Callable<T>> tasks)
            throws InterruptedException {
        return scheduler.invokeAll(tasks);
    }

    public final <T> T invokeAny(Collection<? extends Callable<T>> tasks,
            long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        return scheduler.invokeAny(tasks, timeout, unit);
    }

    public final <T> T invokeAny(Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return scheduler.invokeAny(tasks);
    }

    public final boolean isShutdown() {
        return scheduler.isShutdown();
    }

    public final boolean isTerminated() {
        return scheduler.isTerminated();
    }

    public final <V> ScheduledFuture<V> schedule(Callable<V> callable,
            long delay, TimeUnit unit) {
        return scheduler.schedule(callable, delay, unit);
    }

    public final ScheduledFuture<?> schedule(Runnable command, long delay,
            TimeUnit unit) {
        return scheduler.schedule(command, delay, unit);
    }

    public final ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
            long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(command, initialDelay, period,
                unit);
    }

    public final ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
            long initialDelay, long delay, TimeUnit unit) {
        return scheduler.scheduleWithFixedDelay(command, initialDelay, delay,
                unit);
    }

    public final void shutdown() {
        scheduler.shutdown();
    }

    public final List<Runnable> shutdownNow() {
        return scheduler.shutdownNow();
    }

    public final <T> Future<T> submit(Callable<T> task) {
        return scheduler.submit(task);
    }

    public final <T> Future<T> submit(Runnable task, T result) {
        return scheduler.submit(task, result);
    }

    public final Future<?> submit(Runnable task) {
        return scheduler.submit(task);
    }
}
