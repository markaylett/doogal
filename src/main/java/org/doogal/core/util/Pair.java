package org.doogal.core.util;

import net.jcip.annotations.Immutable;

@Immutable
public final class Pair<T extends Comparable<? super T>, U extends Comparable<? super U>>
        implements Comparable<Pair<T, U>> {
    private final T first;
    private final U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public final int hashCode() {
        return first.hashCode() ^ second.hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof Pair) {
            final Pair rhs = (Pair) obj;
            return first.equals(rhs.first) && second.equals(rhs.second);
        }
        return super.equals(obj);
    }

    @Override
    public final String toString() {
        return String.format("(%s, %s)", first, second);
    }

    public final int compareTo(Pair<T, U> rhs) {
        int n = first.compareTo(rhs.first);
        if (0 == n)
            n = second.compareTo(rhs.second);
        return n;
    }

    public final T getFirst() {
        return first;
    }

    public final U getSecond() {
        return second;
    }
}
