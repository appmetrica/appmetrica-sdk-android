package io.appmetrica.analytics.coreutils.internal.logger;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import io.appmetrica.analytics.coreutils.internal.ApiKeyUtils;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;

public abstract class LoggerStorage {

    private static Map<String, PublicLogger> sPublicLoggers = new HashMap<String, PublicLogger>();
    private static final Object sPublicLock = new Object();
    private static volatile PublicLogger mainPublicLogger = PublicLogger.getAnonymousInstance();

    @NonNull
    public static PublicLogger getMainPublicOrAnonymousLogger() {
        return mainPublicLogger;
    }

    @NonNull
    public static PublicLogger getOrCreateMainPublicLogger(@NonNull String apiKey) {
        mainPublicLogger = getOrCreatePublicLogger(apiKey);
        return mainPublicLogger;
    }

    @NonNull
    public static PublicLogger getOrCreatePublicLogger(@Nullable String apiKey) {
        if (TextUtils.isEmpty(apiKey)) {
            return PublicLogger.getAnonymousInstance();
        }
        PublicLogger logger = sPublicLoggers.get(apiKey);
        if (logger == null) {
            synchronized (sPublicLock) {
                logger = sPublicLoggers.get(apiKey);
                if (logger == null) {
                    logger = new PublicLogger(ApiKeyUtils.createPartialApiKey(apiKey));
                    sPublicLoggers.put(apiKey, logger);
                }
            }
        }
        return logger;
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public static void unsetPublicLoggers() {
        sPublicLoggers = new HashMap<String, PublicLogger>();
        mainPublicLogger = PublicLogger.getAnonymousInstance();
    }
}
