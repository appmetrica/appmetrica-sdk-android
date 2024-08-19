package io.appmetrica.analytics.coreutils.internal;

import android.content.ContentValues;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import kotlin.collections.ArraysKt;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for string operations.
 */
public final class StringUtils {

    private static final String TAG = "[StringUtils]";

    public static final String EMPTY = "";
    public static final String COMMA = ",";
    public static final String PROCESS_POSTFIX_DELIMITER = ":";
    public static final String UNDEFINED = "undefined";

    private static final String HEX_SYMBOLS = "0123456789abcdef";

    private StringUtils() {}

    public static boolean equalsNullSafety(@Nullable String a, @Nullable String b) {
        if (a == null && b == null) {
            return true;
        } else if (a != null && b != null) {
            return a.equals(b);
        } else {
            return false;
        }
    }

    /**
     * Checks that the string is null and if so, returns a def-value.
     */
    public static String ifIsNullToDef(final String value, final String defValue) {
        return null == value ? defValue : value;
    }

    public static String ifIsEmptyToDef(final String value, final String defValue) {
        return TextUtils.isEmpty(value) ? defValue : value;
    }

    public static String emptyIfNull(final String value) {
        return ifIsNullToDef(value, EMPTY);
    }

    public static String capitalize(final String str) {
        if (TextUtils.isEmpty(str)) {
            return EMPTY;
        }

        final char firstChar = str.charAt(0);
        if (Character.isUpperCase(firstChar)) {
            return str;
        } else {
            return Character.toUpperCase(firstChar) + str.substring(1);
        }
    }

    public static byte[] getUTF8Bytes(@Nullable String value) {
        if (value != null) {
            try {
                return value.getBytes("UTF-8");
            } catch (Throwable e) {
                DebugLogger.INSTANCE.error(TAG, e, e.getMessage());
            }
        }
        return new byte[0];
    }

    public static int getUtf8BytesLength(@Nullable String value) {
        return getUTF8Bytes(value).length;
    }

    public static byte[][] getUTF8Bytes(@Nullable List<String> value) {
        byte[][] result = new byte[][]{};
        if (value != null) {
            result = new byte[value.size()][];
            for (int i = 0; i < value.size(); i ++) {
                result[i] = getUTF8Bytes(value.get(i));
            }
        }
        return result;
    }

    public static final String wrapFeatures(String...features) {
        return TextUtils.join(COMMA, features);
    }

    @NonNull
    public static byte[] stringToBytesForProtobuf(@Nullable String value) {
        return value == null ? new byte[] {} : value.getBytes();
    }

    public static String toHexString(@NonNull byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (int b : bytes) {
            builder.append(HEX_SYMBOLS.charAt((b & 0xF0) >> 4));
            builder.append(HEX_SYMBOLS.charAt(b & 0x0F));
        }
        return builder.toString();
    }

    public static String formatSha1(@NonNull byte[] bytes) {
        return toHexString(bytes).toUpperCase(Locale.US).replaceAll("(.{2})(?=.+)", "$1:");
    }

    public static byte[] hexToBytes(@NonNull String hexString) {
        if ((hexString.length() % 2) != 0) {
            throw new IllegalArgumentException("Input string must contain an even number of characters");
        }
        final int len = hexString.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    public static int compare(@Nullable String left, @Nullable String right) {
        if (left == null) {
            return right == null ? 0 : -1;
        } else if (right == null) {
            return 1;
        } else {
            return left.compareTo(right);
        }
    }

    @NonNull
    public static String contentValuesToString(@Nullable ContentValues cv) {
        return cv == null ? "null" : cv.toString();
    }

    @NonNull
    public static String correctIllFormedString(@NonNull String value) {
        return new String(value.getBytes(StandardCharsets.UTF_8));
    }

    @NonNull
    public static String throwableToString(@NonNull Throwable throwable) {
        return throwable.getClass().getName() + ": " + throwable.getMessage() + "\n"
            + ArraysKt.joinToString(
            throwable.getStackTrace(),
            "\n",
            "",
            "",
            -1,
            "...",
            null
        );
    }
}
