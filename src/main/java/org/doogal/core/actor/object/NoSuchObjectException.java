package org.doogal.core.actor.object;

public final class NoSuchObjectException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NoSuchObjectException(String message) {
        super(message);
    }

    public NoSuchObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
