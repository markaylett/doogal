package org.doogal.notes.actor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.doogal.core.actor.ActorPool;
import org.doogal.core.actor.message.Mailboxes;
import org.doogal.core.actor.util.Selector;

final class Main {

    public static void main(String[] args) throws Exception {
        final Mailboxes mailboxes = new Mailboxes();
        final ActorPool pool = new ActorPool(mailboxes);
        try {
            mailboxes.add(NotesActor.class);
            mailboxes.add(ViewActor.class);
            final List<Future<Object>> pending = new ArrayList<Future<Object>>();
            pending.add(pool.add(NotesActor.class));
            pending.add(pool.add(ViewActor.class));
            final List<Future<Object>> done = new ArrayList<Future<Object>>();
            final Selector<Object> selector = mailboxes.newSelector();
            try {
                do
                    selector.wait(pending, done);
                while (!pending.isEmpty());
            } finally {
                selector.destroy();
            }
            for (final Future<Object> future : done)
                future.get();
            Thread.sleep(5000);
        } finally {
            mailboxes.destroy();
            pool.destroy();
        }
    }
}
