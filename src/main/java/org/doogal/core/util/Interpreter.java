package org.doogal.core.util;

public interface Interpreter {

    void eval(String cmd, Object... args) throws EvalException;

    void eval() throws EvalException;
}