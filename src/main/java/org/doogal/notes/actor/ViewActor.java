package org.doogal.notes.actor;

import java.util.concurrent.ExecutionException;

import org.doogal.core.actor.Actor;
import org.doogal.core.actor.message.Mailbox;
import org.doogal.core.actor.object.ObjectBroker;

public final class ViewActor implements Actor {
    @SuppressWarnings("unused")
    private final String name;
    private final ObjectBroker broker;

    public ViewActor(String name, ObjectBroker broker) {
        this.name = name;
        this.broker = broker;
    }

    public final void run(Mailbox mailbox) {
        final Mailbox bar = (Mailbox) broker.getObject(NotesActor.class);
        try {
            for (int i = 0; i < 5; ++i)
                System.out
                        .println(bar
                                .send(
                                        1,
                                        new String[] { "request from view",
                                                "part two" }).get());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }
    }
}
