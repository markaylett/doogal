package org.doogal;

final class ResetException extends EvalException {
    private static final long serialVersionUID = 1L;

    ResetException() {
        super("reset");
    }
}
