package org.doogal.core;

public interface Controller {
    void exit() throws ExitException;
    void ready();
}
