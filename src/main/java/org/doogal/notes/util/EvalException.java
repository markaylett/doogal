package org.doogal.notes.util;

public class EvalException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EvalException(String message) {
        super(message);
    }

    public EvalException(String message, Throwable cause) {
        super(message, cause);
    }
}
