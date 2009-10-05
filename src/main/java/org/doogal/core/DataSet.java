package org.doogal.core;

import java.io.Closeable;
import java.io.IOException;

public interface DataSet extends Closeable {
    
    String get(int i) throws IOException;

    int size();
}
