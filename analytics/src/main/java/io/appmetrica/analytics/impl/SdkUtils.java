package io.appmetrica.analytics.impl;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import java.util.regex.Pattern;

public class SdkUtils {

    public static final String APPMETRICA_TAG = "AppMetrica";
    public static final String APPMETRICA_ATTRIBUTION_TAG = "AppMetrica-Attribution";

    private static final Pattern PUSH_PATTERN = Pattern.compile(".*at io\\.appmetrica\\.analytics\\.push\\.*");
    private static final Pattern APPMETRICA_NOT_PUSH = Pattern.compile(".*at io\\.appmetrica\\.analytics\\.(?!push)");

    // Logs sdk info.
    static void logSdkInfo() {
        final String logInfo = "Initializing of AppMetrica" +
                ", " + StringUtils.capitalize(BuildConfig.BUILD_TYPE) + " type" +
                ", Version " + BuildConfig.VERSION_NAME +
                ", API Level " + AppMetrica.getLibraryApiLevel() +
                ", Dated " + BuildConfig.BUILD_DATE + ".";
        Log.i(APPMETRICA_TAG, logInfo);
    }

    public static void logAttribution(@NonNull String message, Object... arguments) {
        Log.i(APPMETRICA_ATTRIBUTION_TAG, String.format(message, arguments));
    }

    public static void logAttributionW(@NonNull String message, Object... arguments) {
        Log.w(APPMETRICA_ATTRIBUTION_TAG, String.format(message, arguments));
    }

    public static void logAttributionE(@NonNull Throwable ex, @NonNull String message, Object... arguments) {
        Log.e(APPMETRICA_ATTRIBUTION_TAG, String.format(message, arguments), ex);
    }

    public static void logStubUsage() {
        Log.i(APPMETRICA_TAG, "User is locked. So use stubs. Events will not be sent.");
    }

    public static boolean isExceptionFromMetrica(@Nullable Throwable exception) {
        String stackTrace = Utils.getStackTrace(exception);

        return !TextUtils.isEmpty(stackTrace)
                && APPMETRICA_NOT_PUSH.matcher(stackTrace).find();
    }

    public static boolean isExceptionFromPushSdk(@Nullable Throwable exception) {
        String stackTrace = Utils.getStackTrace(exception);

        return !TextUtils.isEmpty(stackTrace)
                && PUSH_PATTERN.matcher(stackTrace).find();
    }

}
