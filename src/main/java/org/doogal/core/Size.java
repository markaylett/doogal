package org.doogal.core;

public final class Size extends Number {
    private static final long KILO = 1024L;
    private static final long MEGA = 1048576L;
    private static final long GIGA = 1073741824L;
    private static final long serialVersionUID = 1L;
    final Long value;
    public Size(long l) {
        this.value = Long.valueOf(l);
    }

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

    public final int intValue() {
        return value.intValue();
    }

    public final long longValue() {
        return value.longValue();
    }

    public final float floatValue() {
        return value.floatValue();
    }

    public final double doubleValue() {
        return value.doubleValue();
    }
}
