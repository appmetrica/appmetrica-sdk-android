package io.appmetrica.analytics.impl.utils.limitation;

public class CollectionLimitation {

    private final int mMaxSize;

    public CollectionLimitation(int maxSize) {
        mMaxSize = maxSize;
    }

    public int getMaxSize() {
        return mMaxSize;
    }

}
