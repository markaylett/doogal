package org.doogal.core.table;

public enum TableType {
    ALIAS(null, new String[] { "unalias" }), BUILTIN("help",
            new String[] { "help" }), DOCUMENT("peek", new String[] { "open",
            "delete", "more", "peek", "publish", "tidy" }), ENVIRONMENT, FIELD_NAME, FIELD_VALUE;
    private final String action;
    private final String[] actions;

    private TableType(String action, String[] actions) {
        this.action = action;
        this.actions = actions;
    }

    private TableType() {
        this(null, new String[] {});
    }

    public final String getAction() {
        return action;
    }

    public final String[] getActions() {
        return actions;
    }
}
