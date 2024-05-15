package io.appmetrica.analytics.coreutils.internal.parsing;

import androidx.annotation.Nullable;

import io.appmetrica.analytics.logger.internal.DebugLogger;

public class ParseUtils {

    private static final String  TAG = "[ParseUtils]";

    public static final int NEGATIVE_INT = -1;
    public static final int ZERO = 0;

    public static int parseInt(String intString, int defaultValue) {
        try {
            return intString == null ? defaultValue : Integer.parseInt(intString);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    public static long parseLong(final String longString, final long defaultValue) {
        try {
            return Long.parseLong(longString);
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    @Nullable
    public static Long parseLong(final String longString) {
        try {
            return Long.parseLong(longString);
        } catch (NumberFormatException e) {
            DebugLogger.error(TAG, e, e.getMessage());
        }
        return null;
    }

    @Nullable
    public static Float parseFloat(final String floatString) {
        try {
            return Float.parseFloat(floatString);
        } catch (NumberFormatException e) {
            DebugLogger.error(TAG, e, e.getMessage());
        }
        return null;
    }

    @Nullable
    public static Integer parseInt(final String intString) {
        try {
            return Integer.parseInt(intString);
        } catch (NumberFormatException e) {
            DebugLogger.error(TAG, e, e.getMessage());
        }
        return null;
    }

    @Nullable
    public static Integer intValueOf(@Nullable String intString) {
        if (intString != null) {
            try {
                return Integer.valueOf(intString);
            } catch (NumberFormatException e) {
                DebugLogger.error(TAG, e);
            }
        }
        return null;
    }

    public static int parseIntOrZero(String intString) {
        return parseInt(intString, ZERO);
    }

    public static int parseIntOrNegative(String intString) {
        return parseInt(intString, NEGATIVE_INT);
    }

}
