package org.doogal.core.actor.message;

import org.doogal.core.actor.concurrent.FutureValue;

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
