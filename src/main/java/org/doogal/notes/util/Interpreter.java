package org.doogal.notes.util;

public interface Interpreter {

    void eval(String cmd, Object... args);

    void eval();
}