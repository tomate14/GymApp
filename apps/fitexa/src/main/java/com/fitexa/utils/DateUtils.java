package com.fitexa.utils;

import java.time.LocalDate;

public final class DateUtils {
    private DateUtils() {}

    public static LocalDate plusOneMonth(LocalDate date) {
        return date.plusMonths(1);
    }

    public static boolean overlaps(LocalDate startA, LocalDate endA, LocalDate startB, LocalDate endB) {
        return !startA.isAfter(endB) && !startB.isAfter(endA);
    }
}
