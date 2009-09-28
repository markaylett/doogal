package org.doogal.core;

abstract class AbstractAlias implements Command {
    public final Type getType() {
        return Type.ALIAS;
    }
}
