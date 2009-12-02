package org.doogal.core.actor;

import java.util.HashMap;
import java.util.Map;

import org.doogal.core.util.Destroyable;

public final class SimpleObjectManager implements ObjectManager {

    private final Map<String, Object> objects;

    public SimpleObjectManager() {
        objects = new HashMap<String, Object>();
    }

    public static String toName(Class<?> clazz) {
        final String name = clazz.getSimpleName();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    public final void destroy() {
        // Destroy all Destroyables.
        Throwable first = null;
        for (final Object value : objects.values())
            if (value instanceof Destroyable) {
                final Destroyable d = (Destroyable) value;
                try {
                    d.destroy();
                } catch (final Throwable t) {
                    if (null == first)
                        first = t;
                }
            }
        if (null != first)
            if (first instanceof RuntimeException)
                throw (RuntimeException) first;
            else if (first instanceof Error)
                throw (Error) first;
    }

    public final Object getObject(String name) {
        return objects.get(name);
    }

    public final Object getObject(Class<?> clazz) {
        return objects.get(toName(clazz));
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
