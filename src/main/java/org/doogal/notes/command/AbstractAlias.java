package org.doogal.notes.command;

public abstract class AbstractAlias implements Command {
    public final String getLargeIcon() {
        return null;
    }

    public final String getSmallIcon() {
        return null;
    }

    public final CommandType getType() {
        return CommandType.ALIAS;
    }
}
