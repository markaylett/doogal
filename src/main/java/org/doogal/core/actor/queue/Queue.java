package org.doogal.core.actor.queue;

import java.util.List;

import org.doogal.core.actor.util.BinaryFunction;
import org.doogal.core.actor.util.UnaryFunction;

public final class Queue {
    private Queue() {
    }

    public static <T, O extends BinaryFunction<ListReference<T>, ListReference<T>, Integer>, P extends UnaryFunction<T, Boolean>> O enqueue2(
            T x, List<T> xs, O op, P pred) {
        assert pred.call(x);
        xs.add(0, x);
        int lhs = 0, rhs = 0, end = xs.size(), fl;
        assert !xs.isEmpty();
        do {
            assert lhs == rhs;
            do
                if (++rhs == end)
                    return op;
            while (!pred.call(xs.get(rhs)));
            fl = op.call(new ListReference<T>(xs, lhs), new ListReference<T>(
                    xs, rhs));
            if (Constants.FOLDL == (fl & Constants.FOLDL)) {
                xs.remove(lhs);
                --rhs;
                --end;
            }
            lhs = rhs;
        } while (Constants.RECUR == (fl & Constants.RECUR));
        return op;
    }

    public static <T> void enqueue(T x, List<T> xs,
            BinaryFunction<ListReference<T>, ListReference<T>, Integer> op,
            UnaryFunction<T, Boolean> pred) {
        assert pred.call(x);
        xs.add(0, x);
        int lhs = 0, rhs = 0, end = xs.size(), fl;
        assert !xs.isEmpty();
        do {
            assert lhs == rhs;
            do
                if (++rhs == end)
                    return;
            while (!pred.call(xs.get(rhs)));
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
