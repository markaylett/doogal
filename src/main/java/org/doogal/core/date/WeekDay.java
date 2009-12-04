package org.doogal.core.date;

public enum WeekDay {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;
    public static WeekDay valueOfJulian(int jd) {
        return WeekDay.values()[(jd + 1) % 7];
    }
}
