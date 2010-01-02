package org.doogal.core.actor.queue;

import java.util.List;

import org.doogal.core.actor.util.BinaryFunction;
import org.doogal.core.actor.util.UnaryPredicate;

public final class Queue {
    private Queue() {
    }

    public static <T> void enqueue(T x, List<T> xs,
            BinaryFunction<ListReference<T>, ListReference<T>, Integer> op,
            UnaryPredicate<T> pred) {
        // The predicate must hold for the element being appended.
        assert pred.call(x);
        xs.add(0, x);
        int lhs = 0, rhs = 0, end = xs.size(), fl;
        assert !xs.isEmpty();
        do {
            assert lhs == rhs;
            // Advance rhs until either it reaches the end, or the predicate
            // holds for an item.
            do
                if (++rhs == end)
                    return;
            while (!pred.call(xs.get(rhs)));
            // Execute operation on lhs and rhs.
            fl = op.call(new ListReference<T>(xs, lhs), new ListReference<T>(
                    xs, rhs));
            if (Constants.FOLDL == (fl & Constants.FOLDL)) {
                xs.remove(lhs);
                --rhs;
                --end;
            }
            lhs = rhs;
        } while (Constants.RECUR == (fl & Constants.RECUR));
    }

    public static <T> void enqueue(T x, List<T> xs,
            BinaryFunction<ListReference<T>, ListReference<T>, Integer> op) {
        enqueue(x, xs, op, Functional.<T> all());
    }

    public static <T> void enqueue(T x, List<T> xs) {
        enqueue(x, xs, Functional.<T> pushBack(), Functional.<T> all());
    }
}
