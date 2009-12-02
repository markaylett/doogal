package org.doogal.core.actor;

import org.doogal.core.util.Destroyable;

public interface ObjectManager extends Destroyable, ObjectBroker {

    void registerObject(String name, Object object);

    void registerObject(Class<?> clazz, Object object);

    void registerObject(Object object);
}
