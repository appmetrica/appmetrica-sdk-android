package io.appmetrica.analytics.location.impl;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;

public class LocationCacheConsumer implements Consumer<Location> {
    private static final String TAG = "[LocationCacheConsumer]";

    @NonNull
    private final LocationDataCache locationDataCache;

    public LocationCacheConsumer(@NonNull LocationDataCache locationDataCache) {
        this.locationDataCache = locationDataCache;
    }

    @Override
    public void consume(@Nullable Location location) {
        DebugLogger.INSTANCE.info(TAG, "Consume: %s", location);
        if (location != null) {
            locationDataCache.updateData(location);
        }
    }
}
