package io.appmetrica.analytics.impl.utils;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import java.util.Random;

public class LongRandom {

    @NonNull
    private final Random mRandom;

    public LongRandom() {
        this(new Random());
    }

    public LongRandom(@NonNull Random random) {
        mRandom = random;
    }

    public long nextValue(long start, long end) {
        if (start >= end) {
            throw new IllegalArgumentException("min should be less than max");
        }

        long seed = mRandom.nextLong();
        seed = seed == Long.MIN_VALUE ? 0 : seed < 0 ? -seed : seed;
        seed = seed % (end - start);

        return start + seed;
    }

    @VisibleForTesting
    @NonNull
    Random getRandom() {
        return mRandom;
    }
}
