package org.doogal.core;

abstract class AbstractBuiltin implements Command {
    public String getLargeIcon() {
        return null;
    }
    public String getSmallIcon() {
        return null;
    }
    public final Type getType() {
        return Type.BUILTIN;
    }
}
