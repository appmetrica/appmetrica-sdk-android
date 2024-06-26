package io.appmetrica.analytics.impl.utils.limitation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.impl.utils.MeasuredJsonMap;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;

public class MapTotalLimitChecker {

    public static final int DEFAULT_MAX_TOTAL_LENGTH = 4500;

    private final String mTag;
    private final int mMaxTotalSize;
    @NonNull
    private final PublicLogger mPublicLogger;

    public MapTotalLimitChecker(final int maxTotalSize, @NonNull String tag, @NonNull PublicLogger logegr) {
        mMaxTotalSize = maxTotalSize;
        mTag = tag;
        mPublicLogger = logegr;
    }

    public boolean willLimitBeReached(@NonNull MeasuredJsonMap json, @NonNull String key, @Nullable String value) {
        int newLength = json.getKeysAndValuesSymbolsCount();
        if (value != null) {
            newLength += value.length();
        }
        if (json.containsKey(key)) {
            String oldValue = json.get(key);
            if (oldValue != null) {
                newLength -= oldValue.length();
            }
        } else {
            newLength += key.length();
        }
        return newLength > mMaxTotalSize;
    }

    public void logTotalLimitReached(@NonNull String key) {
        mPublicLogger.warning(
            "The %s has reached the total size limit that equals %d symbols. Item with key %s will be ignored",
            mTag,
            mMaxTotalSize,
            key
        );
    }
}
