package org.doogal.core.actor.message;

import java.lang.reflect.InvocationTargetException;

/**
 * A message interpreter.
 * 
 * @author Mark Aylett
 */

public interface Interpreter {
    Object eval(int type, Object request) throws InterruptedException,
            InvocationTargetException, NoSuchMethodException;

    void eval(Message message) throws InterruptedException;
}
