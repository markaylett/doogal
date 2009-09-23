package org.doogal;

abstract class AbstractBuiltin implements Command {
    public final Type getType() {
        return Type.BUILTIN;
    }
}
