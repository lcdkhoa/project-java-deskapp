package com.expensemanager.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Format currency per spec: VND, no decimals, thousands separator (e.g. 1.234.567 đ).
 */
public final class CurrencyUtil {
    private static final DecimalFormat FMT = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.GERMAN));

    public static String format(long amount) {
        return FMT.format(amount) + " đ";
    }

    /** For display: negative amounts as -X đ, positive as +X đ when signing is needed. */
    public static String formatSigned(long amount) {
        if (amount < 0) return "-" + format(-amount);
        return "+" + format(amount);
    }
}
