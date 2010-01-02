package org.doogal.notes.core;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import javax.mail.internet.ParseException;

import org.doogal.core.util.Destroyable;
import org.doogal.notes.command.Command;
import org.doogal.notes.table.TableType;
import org.doogal.notes.util.Interpreter;

public interface Doogal extends Destroyable, Interpreter {

    void batch(Reader reader) throws IOException, ParseException;

    void batch(final File config) throws IOException, ParseException;

    void config() throws IOException, ParseException;

    void setSelection(TableType type, Object... args);

    void clearSelection();

    Map<String, Command> getBuiltins();
}
