package org.doogal.core.actor;

import static org.doogal.core.actor.util.Utility.toName;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.doogal.core.actor.object.ObjectBroker;
import org.doogal.core.actor.object.ObjectManager;
import org.doogal.core.util.Destroyable;

final class SimpleObjectManager implements ObjectManager {

    private final ObjectBroker next;
    private final Map<String, Object> objects;

    SimpleObjectManager(ObjectBroker next) {
        this.next = next;
        objects = new ConcurrentHashMap<String, Object>();
    }

    public final void destroy() {
        for (final Object value : objects.values())
            if (value instanceof Destroyable)
                ((Destroyable) value).destroy();
    }

    public final Object getObject(String name) {
        final Object obj = objects.get(name);
        return null == obj ? next.getObject(name) : obj;
    }

    public final Object getObject(Class<?> clazz) {
        return getObject(toName(clazz));
    }

    public final void registerObject(String name, Object object) {
        objects.put(name, object);
    }

    public final void registerObject(Class<?> clazz, Object object) {
        objects.put(toName(clazz), object);
    }

    public final void registerObject(Object object) {
        objects.put(toName(object.getClass()), object);
    }
}
