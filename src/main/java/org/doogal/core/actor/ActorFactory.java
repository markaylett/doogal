package org.doogal.core.actor;

public interface ActorFactory<T extends Actor> {
    T newActor() throws IllegalAccessException, InstantiationException,
            InterruptedException;
}
