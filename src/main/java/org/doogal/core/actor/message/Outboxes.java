package org.doogal.core.actor.message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.doogal.core.actor.util.UnaryPredicate;

final class Outboxes {
    private final Outbox[] outboxes;

    Outboxes(Outbox[] outboxes) {
        this.outboxes = outboxes;
    }

    public final List<Future<Object>> send(int type, Object request,
            MessageFunction op, UnaryPredicate<Message> pred,
            List<Future<Object>> futures) {
        for (final Outbox outbox : outboxes)
            futures.add(outbox.send(type, request, op, pred));
        return futures;
    }

    public final List<Future<Object>> send(int type, Object request,
            MessageFunction op, UnaryPredicate<Message> pred) {
        return send(type, request, op, pred, new ArrayList<Future<Object>>());
    }

    public final List<Future<Object>> send(int type, Object request,
            MessageFunction op, List<Future<Object>> futures) {
        for (final Outbox outbox : outboxes)
            futures.add(outbox.send(type, request, op));
        return futures;
    }

    public final List<Future<Object>> send(int type, Object request,
            MessageFunction op) {
        return send(type, request, op, new ArrayList<Future<Object>>());
    }

    public final List<Future<Object>> send(int type, Object request,
            List<Future<Object>> futures) {
        for (final Outbox outbox : outboxes)
            futures.add(outbox.send(type, request));
        return futures;
    }

    public final List<Future<Object>> send(int type, Object request) {
        return send(type, request, new ArrayList<Future<Object>>());
    }
}
