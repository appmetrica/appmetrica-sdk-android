package io.appmetrica.analytics.coreutils.internal;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * Utility class, to get @NonNull values.
 */
public class WrapUtils {

    private static final String EMPTY_TAG = "<empty>";
    private static final String NULL_TAG = "<null>";

    @Nullable
    public static <T> T getOrDefaultNullable(@Nullable T value, @Nullable T defaultValue) {
        return value == null ? defaultValue : value;
    }

    @NonNull
    public static String getOrDefaultIfEmpty(@Nullable String value, @NonNull String defaultValue) {
        return TextUtils.isEmpty(value) ? defaultValue : value;
    }

    @Nullable
    public static String getOrDefaultNullableIfEmpty(@Nullable String value, @Nullable String defaultValue) {
        return TextUtils.isEmpty(value) ? defaultValue : value;
    }

    @NonNull
    public static <T> T getOrDefault(@Nullable T value, @NonNull T defaultValue) {
        return getOrDefaultGeneric(value, defaultValue);
    }

    public static long getMillisOrDefault(@Nullable final Long value,
                                          @NonNull final TimeUnit timeUnit,
                                          final long defValueMillis) {
        return value == null ? defValueMillis : timeUnit.toMillis(value);
    }

    @NonNull
    private static <T> T getOrDefaultGeneric(@Nullable T value, @NonNull T defaultValue) {
        return value == null ? defaultValue : value;
    }

    @NonNull
    public static <T> String wrapToTag(@Nullable T value) {
        if (value == null) {
            return NULL_TAG;
        } else if (value.toString().isEmpty()) {
            return EMPTY_TAG;
        } else {
            return value.toString();
        }
    }

    public static double getFiniteDoubleOrDefault(double input, double fallback) {
        return Double.isFinite(input) ? input : fallback;
    }

    public static double getFiniteDoubleOrDefaultNullable(@Nullable Double input, double fallback) {
        return input == null ? fallback : getFiniteDoubleOrDefault(input, fallback);
    }
}
