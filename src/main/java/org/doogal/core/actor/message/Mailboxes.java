package org.doogal.core.actor.message;

import static org.doogal.core.actor.object.Utility.toName;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.doogal.core.actor.object.NoSuchObjectBroker;
import org.doogal.core.actor.object.NoSuchObjectException;
import org.doogal.core.actor.object.ObjectBroker;
import org.doogal.core.actor.util.Selector;
import org.doogal.core.actor.util.UpdateListenerSet;
import org.doogal.core.util.Destroyable;

public final class Mailboxes implements Destroyable, ObjectBroker {

    private final ObjectBroker next;
    private final Map<String, Mailbox> mailboxes;
    private final UpdateListenerSet<Future<Object>> listenerSet;

    public Mailboxes(ObjectBroker next) {
        this.next = next;
        mailboxes = new ConcurrentHashMap<String, Mailbox>();
        listenerSet = new UpdateListenerSet<Future<Object>>();
    }

    public Mailboxes() {
        this(new NoSuchObjectBroker());
    }

    public final void destroy() {
        for (final Mailbox mailbox : mailboxes.values())
            mailbox.destroy();
    }

    public final Object getObject(String name) {
        final Object obj = mailboxes.get(name);
        return null == obj ? next.getObject(name) : obj;
    }

    public final Object getObject(Class<?> clazz) {
        return getObject(toName(clazz));
    }

    public final void add(String name) {
        mailboxes.put(name, new Mailbox(listenerSet));
    }

    public final void add(Class<?> clazz) {
        mailboxes.put(toName(clazz), new Mailbox(listenerSet));
    }

    public final Mailbox getMailbox(String name) {
        final Mailbox mailbox = mailboxes.get(name);
        if (null == mailbox)
            throw new NoSuchObjectException("no such mailbox: " + name);
        return mailbox;
    }

    public final Mailbox getMailbox(Class<?> clazz) {
        return getMailbox(toName(clazz));
    }

    public final Selector<Object> newSelector() {
        return Selector.<Object> newInstance(listenerSet);
    }
}
