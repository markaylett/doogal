package org.doogal.core;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.lucene.index.Term;

/*
 Class<?> getColumnClass(int columnIndex)
 int getColumnCount()
 String getColumnName(int columnIndex)
 int getRowCount()
 Object getValueAt(int rowIndex, int columnIndex)
 */

public interface DocumentSet extends DataSet {

    String peek(Term term, PrintWriter out) throws IOException;

    Summary getSummary(int i) throws IOException;
}
