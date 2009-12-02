package org.doogal.core.actor;

import java.util.concurrent.Future;

import net.jcip.annotations.ThreadSafe;

/**
 * <p>
 * A tag interface representing an {@link Actor} proxy. A proxy is used to make
 * asynchronous calls to an actor. They are suitable for use as "top half"
 * interrupt handlers.
 * </p>
 * 
 * <p>
 * Callback or listener patterns work well when they are invoked on the caller's
 * thread. When invoked on a foreign thread, however, they can easily lead to
 * complex threading models and even deadlocks unless managed with extreme care.
 * </p>
 * 
 * <p>
 * They are best avoided where possible, and restricted to "bottom half"
 * interrupt handlers otherwise. These Interrupt handlers should delegate to a
 * "top half" handler to avoid cross-pollination and thread stalling.
 * </p>
 * 
 * @author Mark Aylett
 */

@ThreadSafe
public interface ActorProxy {
    Future<Object> refresh();
}
