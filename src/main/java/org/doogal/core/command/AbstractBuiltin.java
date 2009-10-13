package org.doogal.core.command;

public abstract class AbstractBuiltin implements Command {
    public String getLargeIcon() {
        return null;
    }

    public String getSmallIcon() {
        return null;
    }

    public final CommandType getType() {
        return CommandType.BUILTIN;
    }
}
