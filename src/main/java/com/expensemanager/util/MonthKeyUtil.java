package com.expensemanager.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * month_key as YYYY-MM per Section 6.
 */
public final class MonthKeyUtil {
    public static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMMM yyyy");

    public static String of(YearMonth ym) {
        return ym.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public static String of(LocalDate d) {
        return of(YearMonth.from(d));
    }

    public static YearMonth parse(String monthKey) {
        return YearMonth.parse(monthKey, DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    public static String toLabel(String monthKey) {
        return parse(monthKey).format(MONTH_LABEL);
    }

    /**
     * Create month key from LocalDate.
     * 
     * @param date the date
     * @return month key in yyyy-MM format
     */
    public static String fromDate(LocalDate date) {
        return of(YearMonth.from(date));
    }
}
