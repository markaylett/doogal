package org.doogal.core.actor;

import java.lang.reflect.InvocationTargetException;

import org.doogal.core.actor.object.ObjectBroker;

public interface ActorFactory {
    Actor newActor(String name, ObjectBroker broker)
            throws IllegalAccessException, InstantiationException,
            InvocationTargetException;
}
