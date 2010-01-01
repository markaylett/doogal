package org.doogal.core.actor.object;

import static org.doogal.core.actor.object.Utility.toName;

public final class NoSuchObjectBroker implements ObjectBroker {
    public final Object getObject(String name) {
        throw new NoSuchObjectException("no such object: " + name);
    }

    public final Object getObject(Class<?> clazz) {
        throw new NoSuchObjectException("no such object: " + toName(clazz));
    }
}
