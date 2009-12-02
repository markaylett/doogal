package org.doogal.core.actor;

/**
 * Interface for dependency injection. A Spring-based object broker would be one
 * possible implementation.
 * 
 * @author Mark Aylett
 */

public interface ObjectBroker {
    Object getObject(String name);

    Object getObject(Class<?> clazz);
}
