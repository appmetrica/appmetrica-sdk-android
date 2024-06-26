package io.appmetrica.analytics.impl.utils.limitation;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.logger.appmetrica.internal.PublicLogger;

public class MapTrimmers {

    public static final int DEFAULT_MAP_MAX_SIZE = 30;
    public static final int DEFAULT_KEY_MAX_LENGTH = 50;
    public static final int DEFAULT_VALUE_MAX_LENGTH = 4000;

    private final StringTrimmer mKeyTrimmer;
    private final StringTrimmer mValueTrimmer;
    private final CollectionLimitation mCollectionLimitation;
    @NonNull private final PublicLogger mPublicLogger;

    private final String mTag;

    public MapTrimmers(
            int mapMaxSize,
            int keyMaxLength,
            int valueMaxLength,
            @NonNull String tag,
            @NonNull PublicLogger logger
            ) {
        this(
                new CollectionLimitation(mapMaxSize),
                new StringTrimmer(keyMaxLength, tag + "map key", logger),
                new StringTrimmer(valueMaxLength, tag + "map value", logger),
                tag,
                logger
        );
    }

    @VisibleForTesting
    MapTrimmers(@NonNull CollectionLimitation limitation,
                @NonNull StringTrimmer keyTrimmer,
                @NonNull StringTrimmer valueTrimmer,
                @NonNull String tag,
                @NonNull PublicLogger logger) {
        mCollectionLimitation = limitation;
        mKeyTrimmer = keyTrimmer;
        mValueTrimmer = valueTrimmer;
        mTag = tag;
        mPublicLogger = logger;
    }

    public StringTrimmer getKeyTrimmer() {
        return mKeyTrimmer;
    }

    public StringTrimmer getValueTrimmer() {
        return mValueTrimmer;
    }

    public CollectionLimitation getCollectionLimitation() {
        return mCollectionLimitation;
    }

    public void logContainerLimitReached(@NonNull String key) {
        mPublicLogger.warning(
            "The %s has reached the limit of %d items. Item with key %s will be ignored",
            mTag,
            mCollectionLimitation.getMaxSize(),
            key
        );
    }
}
