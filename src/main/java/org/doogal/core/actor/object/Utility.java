package org.doogal.core.actor.object;

public final class Utility {
    private Utility() {

    }

    public static String toName(Class<?> clazz) {
        final String name = clazz.getSimpleName();
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
