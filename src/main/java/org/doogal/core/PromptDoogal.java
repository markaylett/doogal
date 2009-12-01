package org.doogal.core;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import javax.mail.internet.ParseException;

import org.doogal.core.command.Command;
import org.doogal.core.table.TableType;
import org.doogal.core.util.EvalException;

public final class PromptDoogal implements Doogal {
    private final Controller controller;
    private final Doogal doogal;
    private int depth;

    public PromptDoogal(Controller controller, Doogal doogal) {
        this.controller = controller;
        this.doogal = doogal;
        depth = 0;
    }

    public final void close() throws IOException {
        doogal.close();
    }

    public final void eval(String cmd, Object... args) throws EvalException {
        ++depth;
        try {
            doogal.eval(cmd, args);
        } finally {
            if (0 == --depth)
                controller.ready();
        }
    }

    public final void eval() throws EvalException {
        ++depth;
        try {
            if (1 == depth)
                doogal.eval("next");
        } finally {
            if (0 == --depth)
                controller.ready();
        }
    }

    public final void batch(Reader reader) throws EvalException, IOException,
            ParseException {
        ++depth;
        try {
            doogal.batch(reader);
        } finally {
            if (0 == --depth)
                controller.ready();
        }
    }

    public final void batch(File config) throws EvalException, IOException,
            ParseException {
        ++depth;
        try {
            doogal.batch(config);
        } finally {
            if (0 == --depth)
                controller.ready();
        }
    }

    public final void config() throws EvalException, IOException,
            ParseException {
        ++depth;
        try {
            doogal.config();
        } finally {
            if (0 == --depth)
                controller.ready();
        }
    }

    public final void setSelection(TableType type, Object... args) {
        doogal.setSelection(type, args);
    }

    public final void clearSelection() {
        doogal.clearSelection();
    }

    public final Map<String, Command> getBuiltins() {
        return doogal.getBuiltins();
    }
}
