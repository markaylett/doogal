package org.doogal.core;

import java.io.IOException;

public interface Interpreter {
    void close() throws IOException;

    void eval(String cmd, Object... args) throws EvalException;

    void eval() throws EvalException;
}