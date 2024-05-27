package io.appmetrica.analytics.impl.utils.limitation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.impl.utils.MeasuredJsonMap;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class SimpleMapLimitation {

    private static final String TAG = "[SimpleMapLimitation]";

    @NonNull
    private final MapTrimmers mLimitation;
    @NonNull
    private final MapTotalLimitChecker mTotalLimitChecker;

    public SimpleMapLimitation(@NonNull PublicLogger logger, @NonNull String tag) {
        this(
                new MapTrimmers(
                        MapTrimmers.DEFAULT_MAP_MAX_SIZE,
                        MapTrimmers.DEFAULT_KEY_MAX_LENGTH,
                        MapTrimmers.DEFAULT_VALUE_MAX_LENGTH,
                        tag,
                        logger),
                new MapTotalLimitChecker(
                        MapTotalLimitChecker.DEFAULT_MAX_TOTAL_LENGTH,
                        tag,
                        logger
                )
        );
    }

    @VisibleForTesting
    SimpleMapLimitation(@NonNull MapTrimmers limitation, @NonNull MapTotalLimitChecker totalLimitChecker) {
        mLimitation = limitation;
        mTotalLimitChecker = totalLimitChecker;
    }

    public boolean tryToAddValue(@Nullable MeasuredJsonMap map, @NonNull String key, @Nullable String value) {
        if (map != null) {
            String trimmedKey = mLimitation.getKeyTrimmer().trim(key);
            String trimmedValue = mLimitation.getValueTrimmer().trim(value);
            if (map.containsKey(trimmedKey)) {
                String old = map.get(trimmedKey);
                if (trimmedValue == null || trimmedValue.equals(old) == false) {
                    return insert(map, trimmedKey, trimmedValue, old);
                }
            } else if (trimmedValue != null) { // do not remove pair, if there was not this pair in last revision.
                return insert(map, trimmedKey, trimmedValue, null);
            }
        }
        return false;
    }

    synchronized boolean insert(@NonNull MeasuredJsonMap map,
                                @NonNull String key,
                                @Nullable String value,
                                @Nullable String oldValue) {
        if (map.size() < mLimitation.getCollectionLimitation().getMaxSize() ||
                (mLimitation.getCollectionLimitation().getMaxSize() == map.size() && map.containsKey(key))) {
            if (mTotalLimitChecker.willLimitBeReached(map, key, value) == false) {
                DebugLogger.INSTANCE.info(
                    TAG,
                    "Will insert pair (%s, %s) to environment %s\n. Old value %s",
                    key,
                    value,
                    toString(),
                    oldValue
                );
                map.put(key, value);
                return true;
            } else {
                mTotalLimitChecker.logTotalLimitReached(key);
            }
        } else {
            DebugLogger.INSTANCE.info(TAG, "Size limit for environment %s was reached.", toString());
            mLimitation.logContainerLimitReached(key);
        }
        return false;
    }
}
