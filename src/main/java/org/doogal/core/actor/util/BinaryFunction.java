package org.doogal.core.actor.util;

public interface BinaryFunction<T, U, V> {
    V call(T lhs, U rhs);
}
