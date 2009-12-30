package org.doogal.core.actor;

import java.util.concurrent.Future;

import org.doogal.core.actor.message.Message;
import org.doogal.core.actor.message.MessageFunction;
import org.doogal.core.actor.util.UnaryFunction;

interface Outbox {
    Future<Object> send(int type, Object request, MessageFunction op,
            UnaryFunction<Message, Boolean> pred);

    Future<Object> send(int type, Object request, MessageFunction op);

    Future<Object> send(int type, Object request);
}
