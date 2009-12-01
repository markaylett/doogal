package org.doogal.core;

import org.doogal.core.util.EvalException;

public final class ExitException extends EvalException {
    private static final long serialVersionUID = 1L;

    public ExitException() {
        super("exit");
    }
}
