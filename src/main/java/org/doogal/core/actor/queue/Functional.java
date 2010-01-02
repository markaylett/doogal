package org.doogal.core.actor.queue;

import org.doogal.core.actor.util.BinaryFunction;
import org.doogal.core.actor.util.UnaryPredicate;

public final class Functional {
    private Functional() {
    }

    /**
     * Swap two list references.
     * 
     * @param <T>
     *            element type.
     * @param lhs
     *            left-hand side.
     * @param rhs
     *            right-hand side.
     */

    public static <T> void swap(ListReference<T> lhs, ListReference<T> rhs) {
        final T tmp = lhs.get();
        lhs.set(rhs.get());
        rhs.set(tmp);
    }

    /**
     * Unary predicate that always returns true.
     * 
     * @param <T>
     *            element type.
     * @return the function.
     */

    public static <T> UnaryPredicate<T> all() {
        return new UnaryPredicate<T>() {
            public final Boolean call(T arg) {
                return true;
            }
        };
    }

    /**
     * Add an element to the back of a queue.
     * 
     * @param <T>
     *            element type.
     * @return the function.
     */

    public static <T> BinaryFunction<ListReference<T>, ListReference<T>, Integer> pushBack() {
        return new BinaryFunction<ListReference<T>, ListReference<T>, Integer>() {
            public final Integer call(ListReference<T> lhs, ListReference<T> rhs) {
                return 0;
            }
        };
    }

    /**
     * Only add an element if the queue is already empty.
     * 
     * @param <T>
     *            element type.
     * @return the function.
     */

    public static <T> BinaryFunction<ListReference<T>, ListReference<T>, Integer> pushEmpty() {
        return new BinaryFunction<ListReference<T>, ListReference<T>, Integer>() {
            public final Integer call(ListReference<T> lhs, ListReference<T> rhs) {
                return Constants.FOLDL;
            }
        };
    }

    /**
     * Add an element to the front of a queue.
     * 
     * @param <T>
     *            element type.
     * @return the function.
     */

    public static <T> BinaryFunction<ListReference<T>, ListReference<T>, Integer> pushFront() {
        return new BinaryFunction<ListReference<T>, ListReference<T>, Integer>() {
            public final Integer call(ListReference<T> lhs, ListReference<T> rhs) {
                swap(lhs, rhs);
                return Constants.RECUR;
            }
        };
    }

    /**
     * Insert an element into an ordered list. Equal elements may coexist.
     * 
     * @param <T>
     *            element type.
     * @return the function.
     */

    public static <T extends Comparable<T>> BinaryFunction<ListReference<T>, ListReference<T>, Integer> insertList() {
        return new BinaryFunction<ListReference<T>, ListReference<T>, Integer>() {
            public final Integer call(ListReference<T> lhs, ListReference<T> rhs) {
                if (lhs.get().compareTo(rhs.get()) <= 0)
                    return 0;
                swap(lhs, rhs);
                return Constants.RECUR;
            }
        };
    }

    /**
     * Insert an element into an ordered set.
     * 
     * @param <T>
     *            element type.
     * @return the function.
     */

    public static <T extends Comparable<T>> BinaryFunction<ListReference<T>, ListReference<T>, Integer> insertSet() {
        return new BinaryFunction<ListReference<T>, ListReference<T>, Integer>() {
            public final Integer call(ListReference<T> lhs, ListReference<T> rhs) {
                final int cmp = lhs.get().compareTo(rhs.get());
                if (cmp < 0)
                    return 0;
                if (0 == cmp)
                    return Constants.FOLDL;
                swap(lhs, rhs);
                return Constants.RECUR;
            }
        };
    }

    /**
     * Fold according to some binary function.
     * 
     * @param <T>
     *            element type.
     * @param op
     *            fold operation.
     * @return the function.
     */

    public static <T extends Comparable<T>> BinaryFunction<ListReference<T>, ListReference<T>, Integer> fold(
            final BinaryFunction<T, T, T> op) {
        return new BinaryFunction<ListReference<T>, ListReference<T>, Integer>() {
            public final Integer call(ListReference<T> lhs, ListReference<T> rhs) {
                rhs.set(op.call(lhs.get(), rhs.get()));
                return Constants.FOLDL | Constants.RECUR;
            }
        };
    }
}
