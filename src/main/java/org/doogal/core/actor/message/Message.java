package org.doogal.core.actor.message;

import net.jcip.annotations.Immutable;

import org.doogal.core.actor.concurrent.FutureValue;

/**
 * A message encapsulates a request/response pair. Where the response is some
 * {@link FutureValue}.
 * 
 * <p>
 * Messages are often passed between actors via a message queue.
 * </p>
 * 
 * <p>
 * It is essential that the response be either immutable, a clone or
 * thread-safe, as they are likely to be accessed by other threads.
 * </p>
 * 
 * @author Mark Aylett
 * 
 */

@Immutable
public final class Message {
    private final int type;
    private final Object request;
    private final FutureValue<Object> response;

    public Message(int type, Object request, FutureValue<Object> response) {
        this.type = type;
        this.request = request;
        this.response = response;
    }

    public final int getType() {
        return type;
    }

    public final Object getRequest() {
        return request;
    }

    public final FutureValue<Object> getResponse() {
        return response;
    }
}
