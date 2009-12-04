package org.doogal.notes.core;

import org.doogal.notes.util.EvalException;

public final class ExitException extends EvalException {
    private static final long serialVersionUID = 1L;

    public ExitException() {
        super("exit");
    }
}
