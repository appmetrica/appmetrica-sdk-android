package io.appmetrica.analytics.impl.utils;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;

public abstract class LoggerStorage {

    private static Map<String, PublicLogger> sPublicLoggers = new HashMap<String, PublicLogger>();
    private static final Object sPublicLock = new Object();

    @NonNull
    public static PublicLogger getOrCreatePublicLogger(@Nullable String apiKey) {
        if (TextUtils.isEmpty(apiKey)) {
            return getAnonymousPublicLogger();
        }
        PublicLogger logger = sPublicLoggers.get(apiKey);
        if (logger == null) {
            synchronized (sPublicLock) {
                logger = sPublicLoggers.get(apiKey);
                if (logger == null) {
                    logger = new PublicLogger(apiKey);
                    sPublicLoggers.put(apiKey, logger);
                }
            }
        }
        return logger;
    }

    @NonNull
    public static PublicLogger getAnonymousPublicLogger() {
        return PublicLogger.getAnonymousInstance();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void unsetPublicLoggers() {
        sPublicLoggers = new HashMap<String, PublicLogger>();
    }
}
