package org.doogal.core;

import java.io.Closeable;

public interface Controller extends Closeable {
    void prompt();
}
