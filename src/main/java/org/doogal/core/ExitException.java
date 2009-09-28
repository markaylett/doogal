package org.doogal.core;

public final class ExitException extends EvalException {
    private static final long serialVersionUID = 1L;

    ExitException() {
        super("exit");
    }
}
