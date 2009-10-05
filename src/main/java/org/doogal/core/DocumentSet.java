package org.doogal.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.mail.MessagingException;

import org.apache.lucene.index.Term;

/*
    Class<?> getColumnClass(int columnIndex)
    int getColumnCount()
    String getColumnName(int columnIndex)
    int getRowCount()
    Object getValueAt(int rowIndex, int columnIndex)
 */

public interface DocumentSet extends DataSet {

    String peek(Term term, PrintWriter out) throws IOException, MessagingException;
    
    Collection<Term> getTerms() throws IOException;
}
