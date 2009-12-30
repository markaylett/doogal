package org.doogal.core.actor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.doogal.core.actor.message.Message;

interface Inbox {
    Message recv() throws InterruptedException;

    Message recv(long timeout, TimeUnit unit) throws InterruptedException,
            TimeoutException;
}
