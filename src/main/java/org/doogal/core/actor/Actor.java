package org.doogal.core.actor;

import java.util.Map;

import net.jcip.annotations.NotThreadSafe;

import org.doogal.core.util.Destroyable;

/**
 * <p>
 * The {@link Actor} facade should encapsulate the course-grain transactions
 * offered by a component.
 * </p>
 * 
 * <p>
 * Implementations can assume single-threaded semantics - promoting a simple,
 * lock-free design, which leads to fewer context switches and fuller cache
 * pipelines.
 * </p>
 * 
 * <p>
 * The {@link Actor} should keep environmental dependencies to a minimum;
 * dependency injection, perhaps via the {@link Actor#init(Map)} method, should
 * be preferred so that the actor can be configured to operate in a sandboxed
 * environment. Deterministic test scaffolding, which may be as simple as a Unix
 * filter, is also trivially added to such actors.
 * </p>
 * 
 * <p>
 * The motivation for this design is similar to that of UI event threads,
 * message queues and COM apartments.
 * </p>
 * 
 * <p>
 * It is essential that all return values are either immutable, clones or
 * thread-safe, as they may be marshaled across to other threads. An
 * {@link ProxyActor} can be used to access an object of this class from another
 * thread.
 * </p>
 * 
 * @author Mark Aylett
 * @see ActorExecutor
 */

@NotThreadSafe
public interface Actor extends Destroyable {
    void init(ObjectBroker broker);

    void refresh();
}
