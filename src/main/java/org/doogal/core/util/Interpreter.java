package org.doogal.core.util;

import org.doogal.core.EvalException;

public interface Interpreter {

    void eval(String cmd, Object... args) throws EvalException;

    void eval() throws EvalException;
}