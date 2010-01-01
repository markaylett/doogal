package org.doogal.core.actor.message;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


interface Inbox {
    Message recv() throws InterruptedException;

    Message recv(long timeout, TimeUnit unit) throws InterruptedException,
            TimeoutException;
}
