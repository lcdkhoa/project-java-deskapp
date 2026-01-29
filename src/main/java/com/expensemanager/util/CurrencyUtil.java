package com.expensemanager.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Format currency per spec: VND, no decimals, thousands separator (e.g.
 * 1.234.567 đ).
 */
public final class CurrencyUtil {
    private static final DecimalFormat FMT = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.GERMAN));

    public static String format(long amount) {
        return FMT.format(amount) + " đ";
    }

    /** Format without currency symbol (for KPI cards). */
    public static String formatNoSymbol(long amount) {
        return FMT.format(amount);
    }

    /**
     * For display: negative amounts as -X đ, positive as +X đ when signing is
     * needed.
     */
    public static String formatSigned(long amount) {
        if (amount < 0)
            return "-" + format(-amount);
        return "+" + format(amount);
    }

    /**
     * Apply thousand separator formatting to a text field.
     * When user types numbers, they are automatically grouped with dots (e.g.,
     * 1.000.000).
     * 
     * @param textField the text field to apply formatting
     */
    public static void applyThousandSeparator(JTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new ThousandSeparatorFilter(textField));
    }

    /**
     * Parse a formatted string back to raw number.
     * Removes all dots, commas, and spaces.
     * 
     * @param text the formatted text (e.g., "1.000.000")
     * @return the raw number string (e.g., "1000000")
     */
    public static String parseRawNumber(String text) {
        if (text == null)
            return "";
        return text.replaceAll("[.,\\s]", "");
    }

    /**
     * DocumentFilter that formats numbers with thousand separators as user types.
     */
    private static class ThousandSeparatorFilter extends DocumentFilter {
        private final JTextField textField;
        private boolean updating = false;

        public ThousandSeparatorFilter(JTextField textField) {
            this.textField = textField;
        }

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (updating) {
                super.insertString(fb, offset, string, attr);
                return;
            }
            // Only allow digits
            String filtered = string.replaceAll("[^0-9]", "");
            if (!filtered.isEmpty()) {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                String newText = currentText.substring(0, offset) + filtered + currentText.substring(offset);
                updateWithFormatting(fb, newText);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (updating) {
                super.replace(fb, offset, length, text, attrs);
                return;
            }
            // Only allow digits
            String filtered = (text == null) ? "" : text.replaceAll("[^0-9]", "");
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String rawCurrent = currentText.replaceAll("[^0-9]", "");

            // Calculate position in raw number
            int rawOffset = countDigitsBefore(currentText, offset);
            int rawLength = countDigitsBetween(currentText, offset, offset + length);

            String newRaw = rawCurrent.substring(0, rawOffset) + filtered +
                    rawCurrent.substring(Math.min(rawOffset + rawLength, rawCurrent.length()));
            updateWithFormatting(fb, newRaw);
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if (updating) {
                super.remove(fb, offset, length);
                return;
            }
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
            String rawCurrent = currentText.replaceAll("[^0-9]", "");

            // Calculate position in raw number
            int rawOffset = countDigitsBefore(currentText, offset);
            int rawLength = countDigitsBetween(currentText, offset, offset + length);

            if (rawLength == 0 && rawOffset > 0) {
                // User pressed backspace on a separator, remove the digit before it
                rawOffset--;
                rawLength = 1;
            }

            String newRaw = rawCurrent.substring(0, rawOffset) +
                    rawCurrent.substring(Math.min(rawOffset + rawLength, rawCurrent.length()));
            updateWithFormatting(fb, newRaw);
        }

        private void updateWithFormatting(FilterBypass fb, String rawNumber) throws BadLocationException {
            updating = true;
            try {
                // Remove leading zeros (except for "0" itself)
                rawNumber = rawNumber.replaceFirst("^0+", "");
                if (rawNumber.isEmpty()) {
                    rawNumber = "";
                }

                String formatted = formatWithSeparators(rawNumber);
                fb.remove(0, fb.getDocument().getLength());
                fb.insertString(0, formatted, null);

                // Set caret to end
                textField.setCaretPosition(formatted.length());
            } finally {
                updating = false;
            }
        }

        private String formatWithSeparators(String raw) {
            if (raw == null || raw.isEmpty())
                return "";
            try {
                long number = Long.parseLong(raw);
                return FMT.format(number);
            } catch (NumberFormatException e) {
                return raw;
            }
        }

        private int countDigitsBefore(String text, int position) {
            int count = 0;
            for (int i = 0; i < position && i < text.length(); i++) {
                if (Character.isDigit(text.charAt(i))) {
                    count++;
                }
            }
            return count;
        }

        private int countDigitsBetween(String text, int start, int end) {
            int count = 0;
            for (int i = start; i < end && i < text.length(); i++) {
                if (Character.isDigit(text.charAt(i))) {
                    count++;
                }
            }
            return count;
        }
    }
}
