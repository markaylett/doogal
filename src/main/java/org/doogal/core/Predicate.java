package org.doogal.core;

interface Predicate<T> {
    boolean call(T arg) throws Exception;
}
