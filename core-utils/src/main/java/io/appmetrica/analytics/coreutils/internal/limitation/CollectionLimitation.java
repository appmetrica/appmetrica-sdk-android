package io.appmetrica.analytics.coreutils.internal.limitation;

public class CollectionLimitation {

    private final int mMaxSize;

    public CollectionLimitation(int maxSize) {
        mMaxSize = maxSize;
    }

    public int getMaxSize() {
        return mMaxSize;
    }

}
