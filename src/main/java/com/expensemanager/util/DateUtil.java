package com.expensemanager.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Parse timestamp strings from DB. Handles ISO-8601 and "yyyy-MM-dd HH:mm:ss[.fraction]".
 */
public final class DateUtil {
    public static Instant parseInstant(String t) {
        if (t == null || t.isEmpty()) return null;
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
}
