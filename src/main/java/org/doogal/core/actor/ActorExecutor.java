package org.doogal.core.actor;

import static org.doogal.core.concurrent.ConcurrentUtil.launderThrowable;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public final class ActorExecutor<T extends Actor> {

    private final ExecutorService executor;
    /**
     * Only accessed from execution thread.
     */
    private T actor;

    /**
     * Construct actor on executor's thread. This approach is compatible with
     * thread-local storage and minimises the need for synchronisation.
     * 
     * @param factory
     *            actor factory.
     * @throws InterruptedException
     */
    public ActorExecutor(final ActorFactory<? extends T> factory)
            throws IllegalAccessException, InstantiationException,
            InterruptedException {
        executor = Executors.newSingleThreadExecutor();
        boolean done = false;
        try {
            // Create on execution thread.
            executor.submit(new Callable<T>() {
                public final T call() throws IllegalAccessException,
                        InstantiationException, InterruptedException {
                    // Actor assignment.
                    actor = factory.newActor();
                    return actor;
                }
            }).get();
            // Actor is now assigned. The get operation implies a memory barrier
            // so the value will now be available to this thread.
            done = true;
        } catch (final ExecutionException e) {
            final Throwable t = e.getCause();
            if (t instanceof IllegalAccessException)
                throw (IllegalAccessException) t;
            else if (t instanceof InstantiationException)
                throw (InstantiationException) t;
            else if (t instanceof InterruptedException)
                throw (InterruptedException) t;
            else
                throw launderThrowable(t);
        } finally {
            if (!done)
                executor.shutdownNow();
        }
    }

    /**
     * Construct or obtain actor on executor's thread using an object broker.
     * 
     * @param broker
     *            object broker.
     * @param name
     *            object name.
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InterruptedException
     */
    public ActorExecutor(final ObjectBroker broker, final String name)
            throws IllegalAccessException, InstantiationException,
            InterruptedException {
        this(new ActorFactory<T>() {
            @SuppressWarnings("unchecked")
            public final T newActor() throws IllegalAccessException,
                    InstantiationException {
                return (T) broker.getObject(name);
            }
        });
    }

    /**
     * Construct or obtain actor on executor's thread using an object broker.
     * 
     * @param broker
     *            object broker.
     * @param clazz
     *            actor's class.
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InterruptedException
     */
    public ActorExecutor(final ObjectBroker broker,
            final Class<? extends T> clazz) throws IllegalAccessException,
            InstantiationException, InterruptedException {
        this(new ActorFactory<T>() {
            @SuppressWarnings("unchecked")
            public final T newActor() throws IllegalAccessException,
                    InstantiationException {
                return (T) broker.getObject(clazz);
            }
        });
    }

    /**
     * Construct actor on executor's thread. Trivial constructor will be used.
     * 
     * @param clazz
     *            actor's class.
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InterruptedException
     */
    public ActorExecutor(final Class<? extends T> clazz)
            throws IllegalAccessException, InstantiationException,
            InterruptedException {
        this(new ActorFactory<T>() {
            public final T newActor() throws IllegalAccessException,
                    InstantiationException {
                return clazz.newInstance();
            }
        });
    }

    /**
     * Close the actor and shutdown executor in single operation.
     * 
     * <p>
     * The latch is released once the executor has been shutdown. This
     * simplifies timeout detection from the {@link LifeCycleManager} thread.
     * </p>
     * 
     * <p>
     * Requests posted after shutdown will receive a
     * {@link RejectedExecutionException} exception.
     * </p>
     * 
     * @param latch
     *            the latch.
     * @return null future.
     */

    public final Future<Object> shutdown(final CountDownLatch latch) {
        return executor.submit(new Callable<Object>() {
            public final Object call() throws InterruptedException {
                assert null != actor;
                try {
                    actor.destroy();
                } finally {
                    // Prevent further messages from reaching the actor after
                    // close.
                    executor.shutdownNow();
                    // Shutdown request is complete.
                    latch.countDown();
                }
                return null;
            }
        });
    }

    /**
     * Initialise the actor.
     * 
     * <p>
     * The latch ensures that init is only invoked when all actors have reached
     * this barrier; all implementations must wait on the latch immediately
     * prior to calling {@link Actor#init()} on the executor's thread.
     * </p>
     * 
     * <p>
     * The latch eradicates any race to call init before another actor operation
     * is invoked.
     * </p>
     * 
     * @param broker
     *            a map of actor proxies, or other objects, that may be bound
     *            and used by the implementation.
     * @param latch
     *            the latch.
     * @return null future.
     */

    public final Future<Object> init(final CountDownLatch latch,
            final ObjectBroker broker) {
        return executor.submit(new Callable<Object>() {
            public final Object call() throws InterruptedException {
                latch.countDown();
                latch.await();
                assert null != actor;
                actor.init(broker);
                return null;
            }
        });
    }

    public final Future<Object> refresh() {
        return executor.submit(new Callable<Object>() {
            public final Object call() {
                assert null != actor;
                actor.refresh();
                return null;
            }
        });
    }

    /**
     * Exposed for use by proxy.
     * 
     * @return the executor.
     */
    public final ExecutorService getExecutorService() {
        return executor;
    }

    /**
     * Exposed for use by proxy.
     * 
     * @return the actor.
     */
    public final T getActor() {
        return actor;
    }
}