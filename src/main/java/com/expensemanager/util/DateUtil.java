package com.expensemanager.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateUtil {
    private static final DateTimeFormatter GROUP_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    public static Instant parseInstant(String t) {
        if (t == null || t.isEmpty())
            return null;
        try {
            return Instant.parse(t);
        } catch (Exception e) {
            try {
                String s = t.substring(0, Math.min(19, t.length())).replace(' ', 'T');
                return LocalDateTime.parse(s).atZone(ZoneId.systemDefault()).toInstant();
            } catch (Exception e2) {
                return null;
            }
        }
    }

    public static String formatDateForGroup(LocalDate d) {
        if (d == null)
            return "";
        LocalDate today = LocalDate.now();
        if (d.equals(today))
            return "Today";
        if (d.equals(today.minusDays(1)))
            return "Yesterday";
        return d.format(GROUP_DATE);
    }
}
