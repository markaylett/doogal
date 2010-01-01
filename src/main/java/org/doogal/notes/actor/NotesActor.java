package org.doogal.notes.actor;

import org.doogal.core.actor.Actor;
import org.doogal.core.actor.annotation.MessageHandler;
import org.doogal.core.actor.message.AbstractInterpreter;
import org.doogal.core.actor.message.Mailbox;
import org.doogal.core.actor.message.Message;
import org.doogal.core.actor.object.ObjectBroker;

public final class NotesActor extends AbstractInterpreter implements Actor {
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final ObjectBroker broker;

    public NotesActor(String name, ObjectBroker broker) {
        this.name = name;
        this.broker = broker;
    }

    public final void run(Mailbox mailbox) {
        for (;;)
            try {
                final Message message = mailbox.recv();
                if (null == message)
                    break;
                eval(message);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
    }

    @MessageHandler(1)
    public final String test(String s) throws InterruptedException {
        System.out.printf("notes: %s\n", s);
        Thread.sleep(1000);
        return "reply from notes";
    }

    @MessageHandler(1)
    public final String test2(String s, String t) throws InterruptedException {
        System.out.printf("notes: %s, %s\n", s, t);
        Thread.sleep(1000);
        return "reply from notes";
    }
}