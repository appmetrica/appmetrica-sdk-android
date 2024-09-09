package io.appmetrica.analytics.impl;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.coreutils.internal.StringUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.ImportantLogger;

public class SdkUtils {

    public static final String APPMETRICA_TAG = "AppMetrica";
    public static final String APPMETRICA_ATTRIBUTION_TAG = "AppMetrica-Attribution";

    // Logs sdk info.
    static void logSdkInfo() {
        final String logInfo = "Initializing of AppMetrica" +
                ", " + StringUtils.capitalize(BuildConfig.BUILD_TYPE) + " type" +
                ", Version " + BuildConfig.VERSION_NAME +
                ", API Level " + AppMetrica.getLibraryApiLevel() +
                ", Dated " + BuildConfig.BUILD_DATE + ".";
        ImportantLogger.INSTANCE.info(APPMETRICA_TAG, logInfo);
    }

    public static void logAttribution(@NonNull String message, Object... arguments) {
        ImportantLogger.INSTANCE.info(APPMETRICA_ATTRIBUTION_TAG, String.format(message, arguments));
    }

    public static void logAttributionE(@NonNull Throwable ex, @NonNull String message, Object... arguments) {
        ImportantLogger.INSTANCE.info(
            APPMETRICA_ATTRIBUTION_TAG,
            String.format(message, arguments) + "\n" + StringUtils.throwableToString(ex)
        );
    }

    public static void logStubUsage() {
        ImportantLogger.INSTANCE.info(
            APPMETRICA_TAG,
            "User is locked. So use stubs. Events will not be sent."
        );
    }

    @NonNull
    public static String formSdkBuildType() {
        StringBuilder builder = new StringBuilder();
        builder.append(BuildConfig.SDK_BUILD_FLAVOR);
        if (!TextUtils.isEmpty(BuildConfig.SDK_DEPENDENCY)) {
            builder.append("_" + BuildConfig.SDK_DEPENDENCY);
        }
        if (!TextUtils.isEmpty(BuildConfig.SDK_BUILD_TYPE)) {
            builder.append("_" + BuildConfig.SDK_BUILD_TYPE);
        }
        return builder.toString();
    }
}
