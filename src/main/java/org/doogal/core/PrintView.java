package org.doogal.core;

import java.io.PrintWriter;

import org.apache.commons.logging.Log;

public final class PrintView implements View {

    private final PrintWriter out;
    private final Log log;

    public PrintView(PrintWriter out, Log log) {
        this.out = out;
        this.log = log;
    }
    
    public final Log getLog() {
        return log;
    }

    public final PrintWriter getOut() {
        return out;
    }

    public final void setResults(Results results) {
    }
}
