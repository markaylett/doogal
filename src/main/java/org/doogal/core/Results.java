package org.doogal.core;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

import org.apache.lucene.index.Term;

/*
    Class<?> getColumnClass(int columnIndex)
    int getColumnCount()
    String getColumnName(int columnIndex)
    int getRowCount()
    Object getValueAt(int rowIndex, int columnIndex)
 */

public interface Results extends Closeable {

    String get(int i) throws IOException;

    Collection<Term> terms() throws IOException;

    int size();
}
