package org.doogal.core.actor.util;

public interface UnaryFunction<T, U> {
    U call(T arg);
}
