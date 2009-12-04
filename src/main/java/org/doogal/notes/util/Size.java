package org.doogal.notes.util;

public final class Size extends Number implements Comparable<Size> {
    private static final long KILO = 1024L;
    private static final long MEGA = 1048576L;
    private static final long GIGA = 1073741824L;
    private static final long serialVersionUID = 1L;
    final Long value;

    public Size(long l) {
        value = Long.valueOf(l);
    }

    @Override
    public final String toString() {
        final long l = value.longValue();
        String s;
        if (GIGA < l)
            s = String.format("%.1fG", (double) l / GIGA);
        else if (MEGA < l)
            s = String.format("%.1fM", (double) l / MEGA);
        else if (KILO < l)
            s = String.format("%.1fK", (double) l / KILO);
        else
            s = value.toString();
        return s;
    }

    @Override
    public final int intValue() {
        return value.intValue();
    }

    @Override
    public final long longValue() {
        return value.longValue();
    }

    @Override
    public final float floatValue() {
        return value.floatValue();
    }

    @Override
    public final double doubleValue() {
        return value.doubleValue();
    }

    public final int compareTo(Size rhs) {
        return value.compareTo(rhs.value);
    }
}
