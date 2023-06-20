package io.appmetrica.analytics.impl.utils;

import androidx.annotation.Nullable;

public class BooleanUtils {

    public static boolean isTrue(@Nullable Boolean input) {
        return Boolean.TRUE.equals(input);
    }

    public static boolean isNotTrue(@Nullable Boolean input) {
        return BooleanUtils.isTrue(input) == false;
    }

    public static boolean isFalse(@Nullable Boolean input) {
        return Boolean.FALSE.equals(input);
    }

    public static boolean isNotFalse(@Nullable Boolean input) {
        return BooleanUtils.isFalse(input) == false;
    }
}
