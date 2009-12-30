package org.doogal.core.actor.util;

public final class Functional {
    private Functional() {
    }

    public static <T> void swap(ListReference<T> lhs, ListReference<T> rhs) {
        final T tmp = lhs.get();
        lhs.set(rhs.get());
        rhs.set(tmp);
    }

    public static <T> UnaryFunction<T, Boolean> all() {
        return new UnaryFunction<T, Boolean>() {
            public final Boolean call(T arg) {
                return true;
            }
        };
    }

    public static <T> BinaryFunction<ListReference<T>, ListReference<T>, Integer> pushBack() {
        return new BinaryFunction<ListReference<T>, ListReference<T>, Integer>() {
            public final Integer call(ListReference<T> lhs, ListReference<T> rhs) {
                return 0;
            }
        };
    }

    public static <T> BinaryFunction<ListReference<T>, ListReference<T>, Integer> pushEmpty() {
        return new BinaryFunction<ListReference<T>, ListReference<T>, Integer>() {
            public final Integer call(ListReference<T> lhs, ListReference<T> rhs) {
                return Constants.FOLDL;
            }
        };
    }

    public static <T> BinaryFunction<ListReference<T>, ListReference<T>, Integer> pushFront() {
        return new BinaryFunction<ListReference<T>, ListReference<T>, Integer>() {
            public final Integer call(ListReference<T> lhs, ListReference<T> rhs) {
                swap(lhs, rhs);
                return Constants.RECUR;
            }
        };
    }

    public static <T extends Comparable<T>> BinaryFunction<ListReference<T>, ListReference<T>, Integer> insertBack() {
        return new BinaryFunction<ListReference<T>, ListReference<T>, Integer>() {
            public final Integer call(ListReference<T> lhs, ListReference<T> rhs) {
                if (lhs.get().compareTo(rhs.get()) <= 0)
                    return 0;
                swap(lhs, rhs);
                return Constants.RECUR;
            }
        };
    }

    public static <T extends Comparable<T>> BinaryFunction<ListReference<T>, ListReference<T>, Integer> insertEmpty() {
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
