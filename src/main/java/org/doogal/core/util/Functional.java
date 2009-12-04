package org.doogal.core.util;

import java.io.IOException;

public final class Functional {
    private Functional() {
    }

    public static <T> UnaryPredicate<T> all() {
        return new UnaryPredicate<T>() {
            public final boolean call(T arg) {
                return true;
            }
        };
    }

    public static <T> UnaryPredicate<T> none() {
        return new UnaryPredicate<T>() {
            public final boolean call(T arg) {
                return false;
            }
        };
    }

    public static <T> UnaryPredicate<T> and(final UnaryPredicate<T> lhs,
            final UnaryPredicate<T> rhs) {
        return new UnaryPredicate<T>() {
            public final boolean call(T arg) throws IOException {
                return lhs.call(arg) && rhs.call(arg);
            }
        };
    }

    public static <T> UnaryPredicate<T> not(final UnaryPredicate<T> pred) {
        return new UnaryPredicate<T>() {
            public final boolean call(T arg) throws IOException {
                return !pred.call(arg);
            }
        };
    }

    public static <T> UnaryPredicate<T> or(final UnaryPredicate<T> lhs,
            final UnaryPredicate<T> rhs) {
        return new UnaryPredicate<T>() {
            public final boolean call(T arg) throws IOException {
                return lhs.call(arg) || rhs.call(arg);
            }
        };
    }
}
