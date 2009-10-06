package org.doogal.core;

public interface Predicate<T> {
    boolean call(T arg) throws Exception;
}
