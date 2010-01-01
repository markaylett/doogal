package org.doogal.core.actor.message;

import org.doogal.core.actor.concurrent.FutureValue;
import org.doogal.core.actor.queue.Constants;
import org.doogal.core.actor.queue.ListReference;

public final class Messages {
    private Messages() {
    }

    public static MessageFunction pushBack(final FutureValue<Object> response) {
        return new MessageFunction() {
            public final Integer call(ListReference<Message> lhs,
                    ListReference<Message> rhs) {
                return 0;
            }

            public final FutureValue<Object> getResponse() {
                return response;
            }
        };
    }

    public static MessageFunction pushEmpty(final FutureValue<Object> response) {
        return new MessageFunction() {
            private FutureValue<Object> inner;
            {
                inner = response;
            }

            public final Integer call(ListReference<Message> lhs,
                    ListReference<Message> rhs) {
                // Use existing future.
                inner = rhs.get().getResponse();
                return Constants.FOLDL;
            }

            public final FutureValue<Object> getResponse() {
                return inner;
            }
        };
    }
}
