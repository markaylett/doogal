package org.doogal.core;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;

import javax.mail.internet.ParseException;

public interface Doogal extends Closeable, Interpreter {

    void batch(Reader reader) throws EvalException, IOException, ParseException;

    void batch(final File config) throws EvalException, IOException,
            ParseException;

    void config() throws EvalException, IOException, ParseException;
}
