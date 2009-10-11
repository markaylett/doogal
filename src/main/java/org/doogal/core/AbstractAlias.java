package org.doogal.core;

abstract class AbstractAlias implements Command {
    public final String getLargeIcon() {
        return null;
    }
    public final String getSmallIcon() {
        return null;
    }
    public final Type getType() {
        return Type.ALIAS;
    }
}
