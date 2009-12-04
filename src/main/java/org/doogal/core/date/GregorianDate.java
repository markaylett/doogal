package org.doogal.core.date;

import static org.doogal.core.date.Julian.gregorianToJulian;
import static org.doogal.core.date.Julian.julianToGregorian;

import java.util.Calendar;
import java.util.TimeZone;

import net.jcip.annotations.Immutable;

@Immutable
public final class GregorianDate implements Comparable<GregorianDate> {

    private static final int[] MONTHS = new int[] { 31, 28, 31, 30, 31, 30, 31,
            31, 30, 31, 30, 31 };

    private final int year;
    // Zero-based month is standard and date arithmetic is simpler.
    private final int month;
    private final int day;

    private static int compare(int i, int j) {
        return i == j ? 0 : i < j ? -1 : 1;
    }

    public GregorianDate(int year, int month, int day) {
        this.year = year;
        this.month = month % 12;
        this.day = Math.min(day, daysInMonth(year, month));
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof GregorianDate) {
            final GregorianDate rhs = (GregorianDate) obj;
            // Start with day as the most likely to differ.
            return day == rhs.day && month == rhs.month && year == rhs.year;
        }
        return super.equals(obj);
    }

    @Override
    public final String toString() {
        return String.format("%04d%02d%02d", year, month + 1, day);
    }

    public final int compareTo(GregorianDate rhs) {
        int n = compare(year, rhs.year);
        if (0 == n) {
            n = compare(month, rhs.month);
            if (0 == n)
                n = compare(day, rhs.day);
        }
        return n;
    }

    public static boolean isLeapYear(int year) {
        return 0 == year % 4 && 0 != year % 100 || 0 == year % 400;
    }

    public static int daysInMonth(int year, int month) {
        return 1 == month && isLeapYear(year) ? 29 : MONTHS[month];
    }

    public final boolean isLeapYear() {
        return isLeapYear(year);
    }

    public final int daysInMonth() {
        return daysInMonth(year, month);
    }

    public final GregorianDate addYears(int n) {
        return new GregorianDate(year + n, month, day);
    }

    public final GregorianDate addMonths(int n) {
        final int total = year * 12 + month + n;
        return new GregorianDate(total / 12, total % 12, day);
    }

    public final GregorianDate addDays(int n) {
        return valueOfJulian(toJulian() + n);
    }

    public final int getYear() {
        return year;
    }

    public final int getMonth() {
        return month;
    }

    public final int getDay() {
        return day;
    }

    public final int toJulian() {
        return gregorianToJulian(year, month, day);
    }

    public final int toIso() {
        return year * 10000 + (month + 1) % 100 * 100 + day % 100;
    }

    public final Calendar toCalendar(TimeZone tz) {
        final Calendar cal = Calendar.getInstance(tz);
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    public final Calendar toCalendar() {
        final Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return toCalendar();
    }

    public final WeekDay toWeekDay() {
        return WeekDay.valueOfJulian(toJulian());
    }

    public static GregorianDate valueOfJulian(int jd) {
        final int[] arr = julianToGregorian(jd);
        return new GregorianDate(arr[0], arr[1], arr[2]);
    }

    public static GregorianDate valueOfIso(int iso) {
        final int year = iso / 10000;
        final int month = iso / 100 % 100;
        final int day = iso % 100;
        return new GregorianDate(year, month - 1, day);
    }

    public static GregorianDate valueOf(String s) {
        return valueOfIso(Integer.valueOf(s));
    }

    public static GregorianDate valueOf(Calendar c) {
        return new GregorianDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c
                .get(Calendar.DATE));
    }
}
