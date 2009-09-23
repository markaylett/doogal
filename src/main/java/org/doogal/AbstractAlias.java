package org.doogal;

abstract class AbstractAlias implements Command {
    public final Type getType() {
        return Type.ALIAS;
    }
}
