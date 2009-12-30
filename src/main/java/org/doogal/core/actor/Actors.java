package org.doogal.core.actor;

import static org.doogal.core.actor.util.Utility.toName;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;

import org.doogal.core.actor.concurrent.FutureValue;
import org.doogal.core.actor.object.ObjectBroker;
import org.doogal.core.util.Destroyable;

public final class Actors implements Destroyable {
    private final Mailboxes mailboxes;
    private final Collection<Thread> threads;

    private final void run(String name, ActorFactory factory, Mailbox mailbox,
            FutureValue<Object> future) {
        Thread.currentThread().setName(name);
        Actor actor;
        try {
            actor = factory.newActor(name, mailboxes);
            assert null != actor;
        } catch (final Throwable t) {
            future.setException(t);
            return;
        }
        future.set(null);
        mailbox.activate();
        actor.run(mailbox);
    }

    public Actors(Mailboxes mailboxes) {
        this.mailboxes = mailboxes;
        threads = new ArrayList<Thread>();
    }

    public final void destroy() {
        for (final Thread thread : threads)
            try {
                thread.join();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
    }

    public final Future<Object> add(final String name,
            final ActorFactory factory) {
        final Mailbox mailbox = mailboxes.getMailbox(name);
        final FutureValue<Object> future = mailbox.newFuture();
        final Thread thread = new Thread(new Runnable() {
            public final void run() {
                Actors.this.run(name, factory, mailbox, future);
            }
        });
        threads.add(thread);
        thread.start();
        return future;
    }

    public final Future<Object> add(Class<?> clazz, ActorFactory factory) {
        return add(toName(clazz), factory);
    }

    public final Future<Object> add(final Class<? extends Actor> clazz)
            throws NoSuchMethodException, SecurityException {
        final Constructor<? extends Actor> cons = clazz.getConstructor(
                String.class, ObjectBroker.class);
        return add(clazz, new ActorFactory() {
            public final Actor newActor(String name, ObjectBroker broker)
                    throws IllegalAccessException, InstantiationException,
                    InvocationTargetException {
                return cons.newInstance(name, broker);
            }
        });
    }
}
