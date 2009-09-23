package org.doogal;

interface Interpreter {
    void eval(String cmd, Object... args) throws ExitException, ResetException;

    void eval() throws ExitException, ResetException;
}