package org.doogal;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.logging.Log;

final class StandardLog implements Log {

    public static final int TRACE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int FATAL = 6;

    private final PrintWriter out;
    private final PrintWriter err;
    private int level;

    private final boolean isEnabled(int level) {
        return this.level <= level;
    }

    private final void log(int level, Object message, Throwable t) {

        final StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(message));

        if (null != t) {
            sb.append(" <");
            sb.append(t.toString());
            sb.append(">");
            final StringWriter sw = new java.io.StringWriter();
            final PrintWriter pw = new java.io.PrintWriter(sw);
            t.printStackTrace(pw);
            pw.close();
            sb.append(sw.toString());
        }

        if (level < ERROR)
            out.println(sb.toString());
        else
            err.println(sb.toString());
    }

    StandardLog(PrintWriter out, PrintWriter err) {
        this.out = out;
        this.err = err;
        setLevel(INFO);
    }

    public final void debug(Object message) {

        if (isEnabled(DEBUG))
            log(DEBUG, message, null);
    }

    public final void debug(Object message, Throwable t) {

        if (isEnabled(DEBUG))
            log(DEBUG, message, t);
    }

    public final void trace(Object message) {

        if (isEnabled(TRACE))
            log(TRACE, message, null);
    }

    public final void trace(Object message, Throwable t) {

        if (isEnabled(TRACE))
            log(TRACE, message, t);
    }

    public final void info(Object message) {

        if (isEnabled(INFO))
            log(INFO, message, null);
    }

    public final void info(Object message, Throwable t) {

        if (isEnabled(INFO))
            log(INFO, message, t);
    }

    public final void warn(Object message) {

        if (isEnabled(WARN))
            log(WARN, message, null);
    }

    public final void warn(Object message, Throwable t) {

        if (isEnabled(WARN))
            log(WARN, message, t);
    }

    public final void error(Object message) {

        if (isEnabled(ERROR))
            log(ERROR, message, null);
    }

    public final void error(Object message, Throwable t) {

        if (isEnabled(ERROR))
            log(ERROR, message, t);
    }

    public final void fatal(Object message) {

        if (isEnabled(FATAL))
            log(FATAL, message, null);
    }

    public final void fatal(Object message, Throwable t) {

        if (isEnabled(FATAL))
            log(FATAL, message, t);
    }

    public final boolean isDebugEnabled() {

        return isEnabled(DEBUG);
    }

    public final boolean isErrorEnabled() {

        return isEnabled(ERROR);
    }

    public final boolean isFatalEnabled() {

        return isEnabled(FATAL);
    }

    public final boolean isInfoEnabled() {

        return isEnabled(INFO);
    }

    public final boolean isTraceEnabled() {

        return isEnabled(TRACE);
    }

    public final boolean isWarnEnabled() {

        return isEnabled(WARN);
    }

    public final void setLevel(int level) {
        this.level = level;
    }

    public final int getLevel() {
        return level;
    }
}
