package org.doogal.core.util;

import java.io.IOException;

public interface UnaryPredicate<T> {
    boolean call(T arg) throws IOException;
}
