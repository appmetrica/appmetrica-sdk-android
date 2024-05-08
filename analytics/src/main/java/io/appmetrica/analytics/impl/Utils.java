package io.appmetrica.analytics.impl;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.impl.crash.client.StackTraceItemInternal;
import io.appmetrica.analytics.logger.internal.YLogger;
import io.appmetrica.analytics.networktasks.internal.NetworkTask;
import java.io.Closeable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

public final class Utils {

    private static final String TAG = "[Utils]";

    private Utils() {}

    public static String getStackTrace(@Nullable Throwable throwable) {
        String stackTrace = StringUtils.EMPTY;

        if (null != throwable) {
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            throwable.printStackTrace(printWriter);
            stackTrace = result.toString();
            printWriter.close();
        }

        return stackTrace;
    }

    @NonNull
    public static StackTraceElement[] getStackTraceSafely(@Nullable Throwable value) {
        if (value != null) {
            try {
                return value.getStackTrace();
            } catch (Throwable ex) {
                YLogger.error(TAG, ex, "Could not get stack trace.");
            }
        }
        return new StackTraceElement[0];
    }

    @NonNull
    public static List<StackTraceItemInternal> convertStackTraceToInternal(@NonNull StackTraceElement[] input) {
        List<StackTraceItemInternal> result = new ArrayList<>();
        for (StackTraceElement element : input) {
            result.add(new StackTraceItemInternal(element));
        }
        return result;
    }

    @NonNull
    public static List<StackTraceItemInternal> convertStackTraceToInternal(@NonNull Iterable<StackTraceElement> input) {
        List<StackTraceItemInternal> result = new ArrayList<>();
        for (StackTraceElement element : input) {
            result.add(new StackTraceItemInternal(element));
        }
        return result;
    }

    /**
     * Closes a {@link java.io.Closeable} object unconditionally.
     *
     * @param closeable {@link java.io.Closeable}
     */
    public static void closeCloseable(final Closeable closeable) {
        try {
            if (null != closeable) {
                closeable.close();
            }
        } catch (Throwable exception) {
            // Do nothing
        }
    }

    /**
     * Closes a {@link android.database.Cursor} object unconditionally.
     *
     * @param cursor {@link android.database.Cursor}
     */
    public static void closeCursor(final Cursor cursor) {
        if (null != cursor && !cursor.isClosed()) {
            cursor.close();
        }
    }

    public static boolean areEqual(Object arg1, Object arg2) {
        if (arg1 == null && arg2 == null) {
            return true;
        }
        if (arg1 == null || arg2 == null) {
            return false;
        }
        return arg1.equals(arg2);
    }

    /**
     * http://stackoverflow.com/questions/5743485/android-resultreceiver-across-packages
     */
    public static ResultReceiver resultReceiverAcrossPackages(ResultReceiver actualReceiver) {
        Parcel parcelObj = Parcel.obtain();
        actualReceiver.writeToParcel(parcelObj, 0);
        parcelObj.setDataPosition(0);
        ResultReceiver resultReceiver = ResultReceiver.CREATOR.createFromParcel(parcelObj);
        parcelObj.recycle();
        return resultReceiver;
    }

    public static void endTransaction(final SQLiteDatabase database) {
        try {
            if (null != database) {
                database.endTransaction();
            }
        } catch (Throwable exception) {
            // Do nothing
        }
    }

    public static void closeDatabase(@Nullable final SQLiteDatabase database) {
        try {
            if (null != database) {
                database.close();
            }
        } catch (Throwable exception) {
            YLogger.error(TAG, exception, "Could not close database.");
        }
    }

    public static boolean isNullOrEmpty(final Map map) {
        return map == null || map.size() == 0;
    }

    public static boolean isNullOrEmpty(final Collection collection) {
        return collection == null || collection.size() == 0;
    }

    public static <T> boolean isNullOrEmpty(@Nullable final T[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable final byte[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable final int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable final long[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isNullOrEmpty(@Nullable final Cursor cursor) {
        return cursor == null || cursor.getCount() == 0;
    }

    public static boolean isApiKeyDefined(final String apiKey) {
        return !TextUtils.isEmpty(apiKey)
            && !CounterConfigurationValues.DEFAULT_UNDEFINED_API_KEY.equals(apiKey);
    }

    @NonNull
    public static String createPartialApiKey(@Nullable String fullApiKey) {
        String ret = StringUtils.EMPTY;
        if (TextUtils.isEmpty(fullApiKey) == false) {
            final int apiKeyPrefixLength = 8;
            final int apiKeySuffixLength = 4;
            final String apiKeyMiddleMask = "-xxxx-xxxx-xxxx-xxxxxxxx";
            final int apiKeyRequiredLength = 36;
            if (fullApiKey.length() == apiKeyRequiredLength) {
                StringBuilder builder = new StringBuilder(fullApiKey);
                builder.replace(apiKeyPrefixLength, fullApiKey.length() - apiKeySuffixLength, apiKeyMiddleMask);
                ret = builder.toString();
            }
        }
        return ret;
    }

    public static boolean isFieldSet(Object field) {
        return field != null;
    }

    public static String[] convertToStringArray(long[] input) {
        String[] output = new String[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = String.valueOf(input[i]);
        }
        return output;
    }

    @Nullable
    public static String trimToSize(@Nullable final String value, final int limit) {
        if (value == null) {
            return null;
        }
        return value.length() > limit ? value.substring(0, limit) : value;
    }

    @SafeVarargs
    public static <T> boolean isAnyNull(final T... values) {
        if (null == values) {
            return false;
        }

        for (T value : values) {
            if (value == null) {
                return true;
            }
        }
        return false;
    }

    public static boolean areAllNullOrEmpty(@Nullable String... values) {
        for (String value : values) {
            if (!TextUtils.isEmpty(value)) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    public static BigDecimal microsToBigDecimal(long micros) {
        return new BigDecimal(micros).divide(new BigDecimal(1000000), 6, BigDecimal.ROUND_HALF_EVEN);
    }

    public static double getFiniteDoubleOrDefault(double input, double fallback) {
        return Double.isNaN(input) || Double.isInfinite(input) ? fallback : input;
    }

    @Nullable
    public static Boolean getBooleanOrNull(@NonNull Bundle bundle, @NonNull String key) {
        if (bundle.containsKey(key)) {
            return bundle.getBoolean(key);
        }
        return null;
    }

    @Nullable
    public static Integer getIntOrNull(@NonNull Bundle bundle, @NonNull String key) {
        if (bundle.containsKey(key)) {
            return bundle.getInt(key);
        }
        return null;
    }

    @Nullable
    public static Application applicationFromContext(@NonNull Context context) {
        Application application = null;
        try {
            application = (Application) context.getApplicationContext();
        } catch (Throwable ex) {
            YLogger.error(TAG, ex, "Context %s is not application", context);
        }
        return application;
    }

    public static boolean isBadRequest(int code) {
        return code == HttpsURLConnection.HTTP_BAD_REQUEST;
    }

    public static NetworkTask.ShouldTryNextHostCondition notIsBadRequestCondition() {
        return new NetworkTask.ShouldTryNextHostCondition() {
            @Override
            public boolean shouldTryNextHost(int responseCode) {
                return !isBadRequest(responseCode);
            }
        };
    }

    @NonNull
    public static String[] joinToArray(@NonNull List<String> initial, @NonNull String... additionalItems) {
        List<String> list = new ArrayList<>(initial);
        list.addAll(Arrays.asList(additionalItems));
        return list.toArray(new String[0]);
    }
}
