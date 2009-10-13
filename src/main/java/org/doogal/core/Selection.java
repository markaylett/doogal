package org.doogal.core;

import org.doogal.core.table.TableType;

final class Selection {
    private final TableType type;
    private final Object[] args;

    Selection(TableType type, Object... args) {
        this.type = type;
        this.args = args;
    }

    final TableType getType() {
        return type;
    }

    final Object getArg() {
        return args[0];
    }

    final Object[] getArgs() {
        return args;
    }
}
