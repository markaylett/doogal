package org.doogal.core;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import javax.mail.internet.ParseException;

import org.doogal.core.command.Command;
import org.doogal.core.table.TableType;
import org.doogal.core.util.EvalException;
import org.doogal.core.util.Interpreter;

public interface Doogal extends Closeable, Interpreter {

    void batch(Reader reader) throws EvalException, IOException, ParseException;

    void batch(final File config) throws EvalException, IOException,
            ParseException;

    void config() throws EvalException, IOException, ParseException;

    void setSelection(TableType type, Object... args);

    void clearSelection();

    Map<String, Command> getBuiltins();
}
