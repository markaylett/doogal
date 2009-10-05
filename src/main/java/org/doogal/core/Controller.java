package org.doogal.core;

public interface Controller {
    void exit(boolean interact) throws ExitException;

    void ready();
}
