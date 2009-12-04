package org.doogal.notes.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import javax.mail.internet.ParseException;

import org.doogal.notes.command.Command;
import org.doogal.notes.table.TableType;
import org.doogal.notes.util.EvalException;

public final class PromptDoogal implements Doogal {
    private final Controller controller;
    private final Doogal doogal;
    private int depth;

    public PromptDoogal(Controller controller, Doogal doogal) {
        this.controller = controller;
        this.doogal = doogal;
        depth = 0;
    }

    public final void destroy() {
        doogal.destroy();
    }

    public final void eval(String cmd, Object... args) {
        ++depth;
        try {
            doogal.eval(cmd, args);
        } finally {
            if (0 == --depth)
                controller.ready();
        }
    }

    public final void eval() {
        ++depth;
        try {
            if (1 == depth)
                doogal.eval("next");
        } finally {
            if (0 == --depth)
                controller.ready();
        }
    }

    public final void batch(Reader reader) throws IOException, ParseException {
        ++depth;
        try {
            doogal.batch(reader);
        } finally {
            if (0 == --depth)
                controller.ready();
        }
    }

    public final void batch(File config) throws IOException, ParseException {
        ++depth;
        try {
            doogal.batch(config);
        } finally {
            if (0 == --depth)
                controller.ready();
        }
    }

    public final void config() throws IOException, ParseException {
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
