package org.doogal.core.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class ConcurrentUtil {
    private static final long TIMEOUT = 5;

    private ConcurrentUtil() {
    }

    public static void destroy(ExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (!executor.isShutdown())
            executor.shutdownNow();
    }

    /**
     * If the Throwable is an Error, throw it; if it is a RuntimeException
     * return it, otherwise throw IllegalStateException
     */

    public static RuntimeException launderThrowable(Throwable t) {
        if (t instanceof RuntimeException)
            return (RuntimeException) t;
        else if (t instanceof Error)
            throw (Error) t;
        else
            throw new IllegalStateException("Not unchecked", t);
    }
}
