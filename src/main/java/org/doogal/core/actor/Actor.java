package org.doogal.core.actor;

import org.doogal.core.actor.message.Mailbox;

/**
 * Actors are confined to a single thread. Communications between actors are
 * conducted via message queues.
 * 
 * <p>
 * This design promotes a simple, lock-free design, which should lead to fewer
 * context switches and fuller cache pipelines.
 * </p>
 * 
 * <p>
 * Actors are similar in nature to UI event threads and COM apartments.
 * </p>
 * 
 * @author Mark Aylett
 * 
 */
public interface Actor {
    void run(Mailbox mailbox);
}
