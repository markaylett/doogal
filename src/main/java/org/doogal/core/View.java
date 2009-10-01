package org.doogal.core;

import java.io.PrintWriter;

import org.apache.commons.logging.Log;

public interface View {
    PrintWriter getOut();
    Log getLog();
}
