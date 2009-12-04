package org.doogal.core.date;

public final class Julian {

    private static final int YDAYS1 = 365;
    private static final int YDAYS4 = 1461;
    private static final int YDAYS100 = 36524;
    private static final int YDAYS400 = 146097;

    private Julian() {

    }

    /**
     * Adjusted month and year of Gregorian date:
     * 
     * January and February are treated as the thirteenth and fourteenth month
     * of the preceding year, respectively.
     */

    private static int monToAdj(int m) {
        return (m + 10) % 12 + 2;
    }

    private static int yearToAdj(int y, int m) {
        return y - 1 + (m + 8) / 10;
    }

    private static int adjToMon(int m) {
        return (m + 12) % 12;
    }

    private static int adjToYear(int y, int m) {
        return y + m / 12;
    }

    /**
     * No dependency on GregorianDate because GregorianDate depends on this
     * class.
     */

    public static int[] julianToGregorian(int jd) {
        /**
         * Elapsed days since 1 March 4801 BCE (Gregorian).
         */
        final int base = jd + 32044;
        /**
         * Elapsed 400 year cycles.
         */
        final int years400 = base / YDAYS400;
        final int ydays400 = base % YDAYS400;
        /**
         * Elapsed 100 year cycles within 400 year cycle.
         */
        final int years100 = Math.min(3, ydays400 / YDAYS100);
        final int ydays100 = ydays400 - YDAYS100 * years100;
        /**
         * Elapsed 4 year cycles within 100 year cycle.
         */
        final int years4 = ydays100 / YDAYS4;
        final int ydays4 = ydays100 % YDAYS4;
        /**
         * Elapsed year cycles within 4 year cycle.
         */
        final int years1 = Math.min(3, ydays4 / YDAYS1);
        final int ydays1 = ydays4 - YDAYS1 * years1;

        final int mon = (111 * ydays1 + 41) / 3395;
        final int mday = ydays1 - 30 * mon - 7 * (mon + 1) / 12 + 1;
        final int adjmon = mon + 2;
        final int adjyear = 400 * years400 + 100 * years100 + 4 * years4
                + years1 - 4800;

        return new int[] { adjToYear(adjyear, adjmon), adjToMon(adjmon), mday };
    }

    public static int gregorianToJulian(int y, int m, int d) {
        final int adjmon = monToAdj(m);
        final int adjyear = yearToAdj(y, m);

        /**
         * Elapsed 100 year cycles.
         */
        final int years100 = adjyear / 100;
        final int ydays100 = adjyear % 100;

        return YDAYS400 * (years100 / 4) + YDAYS100 * (years100 % 4) + YDAYS4
                * (ydays100 / 4) + YDAYS1 * (ydays100 % 4) + 7 * (adjmon - 1)
                / 12 + 30 * (adjmon + 1) + d + 1721029;
    }
}