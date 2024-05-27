package io.appmetrica.analytics.location.impl;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.annotations.GeoThread;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreutils.internal.time.TimePassedChecker;
import io.appmetrica.analytics.locationapi.internal.LocationFilter;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class SingleProviderLocationFiltrator {

    private static final String TAG = "[SingleProviderLocationFiltrator]";

    @NonNull
    private LocationFilter locationFilter;
    @NonNull
    private final TimePassedChecker timePassedChecker;
    @NonNull
    private final List<Consumer<Location>> consumers = new CopyOnWriteArrayList<>();
    @Nullable
    private Location lastLocation;
    private long lastLocationTimestamp;

    public SingleProviderLocationFiltrator(@NonNull LocationFilter locationFilter) {
        this.locationFilter = locationFilter;
        this.timePassedChecker = new TimePassedChecker();
    }

    @GeoThread
    public void handleLocation(@NonNull Location location) {
        if (shouldHandle(location)) {
            DebugLogger.INSTANCE.info(
                TAG,
                "Should handle and store location for provider: " + location.getProvider()
            );
            updateLastLocation(location);
            consumeLocation(location);
        } else {
            DebugLogger.INSTANCE.info(TAG, "Location %s will be ignored", location);
        }
    }

    @GeoThread
    private void updateLastLocation(@NonNull Location location) {
        lastLocation = location;
        lastLocationTimestamp = System.currentTimeMillis();
    }

    @GeoThread
    private void consumeLocation(@NonNull Location location) {
        for (Consumer<Location> consumer : consumers) {
            consumer.consume(location);
        }
    }

    @GeoThread
    private boolean shouldHandle(@Nullable Location location) {
        boolean shouldHandle = false;
        if (location != null) {
            boolean isSavedLocationOutdated = false;
            boolean isDistanceGreaterThanUpdateInterval = false;
            boolean isLocationNewerOrTheSame = false;
            if (lastLocation != null) {
                isSavedLocationOutdated = isSavedLocationOutdated(locationFilter);
                isDistanceGreaterThanUpdateInterval =
                    isDistanceGreaterThanUpdateInterval(locationFilter, location);
                isLocationNewerOrTheSame = isLocationNewerOrTheSame(location);
                shouldHandle = (isSavedLocationOutdated || isDistanceGreaterThanUpdateInterval)
                    && isLocationNewerOrTheSame;
            } else {
                shouldHandle = true;
            }

            DebugLogger.INSTANCE.info(
                TAG,
                "[LocationHandler] shouldHandle = mLastLocation == null (%s) || " +
                    "(isSavedLocationOutdated(%s) " +
                    "|| isDistanceGreaterThanUpdateInterval(%s) && isLocationNewerOrTheSame(%s)) = %s",
                String.valueOf(lastLocation == null),
                String.valueOf(isSavedLocationOutdated),
                String.valueOf(isDistanceGreaterThanUpdateInterval),
                String.valueOf(isLocationNewerOrTheSame),
                String.valueOf(shouldHandle)
            );
        }
        return shouldHandle;
    }

    @GeoThread
    private boolean isSavedLocationOutdated(@NonNull LocationFilter filter) {
        return timePassedChecker.didTimePassMillis(
            lastLocationTimestamp,
            filter.getUpdateTimeInterval(),
            "isSavedLocationOutdated"
        );
    }

    @GeoThread
    private boolean isDistanceGreaterThanUpdateInterval(@NonNull LocationFilter filter, @NonNull Location location) {
        float distanceChanging = calculateDistanceChanging(location);
        boolean result = distanceChanging > filter.getUpdateDistanceInterval();

        DebugLogger.INSTANCE.info(
            TAG,
            "[LocationHandler] isDistanceGreaterThanUpdateInterval = distanceChanging(%s) > " +
                "updateDistanceInterval(%s) ? %s",
            String.valueOf(distanceChanging),
            String.valueOf(filter.getUpdateDistanceInterval()),
            String.valueOf(result)
        );

        return result;
    }

    private boolean isLocationNewerOrTheSame(@NonNull Location location) {
        boolean result = lastLocation == null || (location.getTime() - lastLocation.getTime() >= 0);
        if (lastLocation == null) {
            DebugLogger.INSTANCE.info(
                TAG,
                "[LocationHandler] isLocationNewerOrTheSame: mLastLocation = null"
            );
        } else {
            DebugLogger.INSTANCE.info(
                TAG,
                "[LocationHandler] isLocationNewerOrTheSame = mLastLocation == null (%s) || " +
                    "(locationTime(%s) - lastLocationTime(%s) = %s >= 0 ? %s)",
                String.valueOf(false),
                String.valueOf(location.getTime()),
                String.valueOf(lastLocation.getTime()),
                String.valueOf(location.getTime() - lastLocation.getTime()),
                String.valueOf(result)
            );
        }

        return result;
    }

    private float calculateDistanceChanging(Location location) {
        return location.distanceTo(lastLocation);
    }

    @GeoThread
    public void setLocationFilter(@NonNull LocationFilter locationFilter) {
        DebugLogger.INSTANCE.info(TAG, "setLocationFilter: %s", locationFilter);
        this.locationFilter = locationFilter;
    }

    @GeoThread
    public void registerConsumer(@NonNull Consumer<Location> consumer) {
        DebugLogger.INSTANCE.info(
            TAG,
            "Register consumer: %s; total consumers = %d",
            consumer,
            consumers.size()
        );
        consumers.add(consumer);
    }
}
