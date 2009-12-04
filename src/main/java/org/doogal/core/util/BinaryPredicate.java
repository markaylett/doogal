package org.doogal.core.util;

import java.io.IOException;

public interface BinaryPredicate<T, U> {
    boolean call(T lhs, U rhs) throws IOException;
}
