package org.doogal.core.actor;

public final class ActorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ActorException(String message) {
        super(message);
    }

    public ActorException(String format, Throwable cause) {
        super(format, cause);
    }
}
