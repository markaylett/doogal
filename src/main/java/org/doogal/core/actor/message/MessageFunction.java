package org.doogal.core.actor.message;

import org.doogal.core.actor.concurrent.FutureValue;
import org.doogal.core.actor.util.BinaryFunction;
import org.doogal.core.actor.util.ListReference;

public interface MessageFunction extends
        BinaryFunction<ListReference<Message>, ListReference<Message>, Integer> {
    FutureValue<Object> getResponse();
}
