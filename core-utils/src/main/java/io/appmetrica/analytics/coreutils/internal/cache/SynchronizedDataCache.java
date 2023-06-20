package io.appmetrica.analytics.coreutils.internal.cache;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class SynchronizedDataCache<T> extends DataCache<T> {

    public SynchronizedDataCache(long refreshTime, long expiryTime, @NonNull String descriptions) {
        super(refreshTime, expiryTime, descriptions);
    }

    @Nullable
    @Override
    public synchronized T getData() {
        return super.getData();
    }

    @Override
    public synchronized void updateData(@NonNull T newData) {
        super.updateData(newData);
    }

    @AnyThread
    @Override
    public synchronized boolean shouldUpdate() {
        return super.shouldUpdate();
    }
}
