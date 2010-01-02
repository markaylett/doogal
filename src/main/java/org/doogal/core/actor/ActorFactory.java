package org.doogal.core.actor;

import java.lang.reflect.InvocationTargetException;

import org.doogal.core.actor.object.ObjectBroker;

/**
 * Factories are used to create actors.
 * 
 * <p>
 * An Actor's dependencies should be injected by the factory. A factory can
 * query these dependencies using the {@link ObjectBroker}.
 * </p>
 * 
 * <p>
 * Dependency injection allows the actor to be configured for a sandboxed
 * environment. Deterministic test scaffolding, which may be as simple as a Unix
 * filter, is also trivially added to such actors.
 * </p>
 * 
 * @author Mark Aylett
 * 
 */
public interface ActorFactory {
    Actor newActor(String name, ObjectBroker broker)
            throws IllegalAccessException, InstantiationException,
            InvocationTargetException;
}
