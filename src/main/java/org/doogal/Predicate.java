package org.doogal;

interface Predicate<T> {
    boolean call(T arg) throws Exception;
}
