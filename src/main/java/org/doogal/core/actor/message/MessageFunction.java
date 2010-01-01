package org.doogal.core.actor.message;

import org.doogal.core.actor.concurrent.FutureValue;
import org.doogal.core.actor.queue.ListReference;
import org.doogal.core.actor.util.BinaryFunction;

public interface MessageFunction extends
        BinaryFunction<ListReference<Message>, ListReference<Message>, Integer> {
    FutureValue<Object> getResponse();
}
