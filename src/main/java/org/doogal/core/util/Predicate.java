package org.doogal.core.util;

public interface Predicate<T> {
    boolean call(T arg) throws Exception;
}
