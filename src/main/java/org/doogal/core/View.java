package org.doogal.core;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;

public interface View extends Pager {
    void setResults(Results results) throws IOException;
    PrintWriter getOut();
    Log getLog();
}
