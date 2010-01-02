package org.doogal.core.actor.message;

import java.util.concurrent.Future;

import org.doogal.core.actor.util.UnaryPredicate;

/**
 * Messages are sent to an Actor via an its Outbox.
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
 * <p>
 * Posting to an Outbox makes an ideal "bottom half" handler, with the Actor
 * naturally representing the "top half".
 * </p>
 * 
 * @author Mark Aylett
 */

interface Outbox {
    Future<Object> send(int type, Object request, MessageFunction op,
            UnaryPredicate<Message> pred);

    Future<Object> send(int type, Object request, MessageFunction op);

    Future<Object> send(int type, Object request);
}
