package io.appmetrica.analytics.location.impl;

import android.location.Location;
import android.location.LocationManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import io.appmetrica.analytics.coreutils.internal.cache.SynchronizedDataCache;
import io.appmetrica.analytics.logger.internal.DebugLogger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LocationDataCache extends SynchronizedDataCache<Location> {

    private static final String TAG = "[LocationDataCache]";

    public static final long DEFAULT_CACHE_TTL = TimeUnit.SECONDS.toMillis(10);
    public static final long OUTDATED_TIME_INTERVAL = TimeUnit.MINUTES.toMillis(2);
    public static final long OUTDATED_ACCURACY = 200;
    public static final long MIN_DISTANCE_DELTA = 50;
    public static final List<String> WHITE_LIST_PROVIDERS = Arrays.asList(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
    );

    public static class Configuration {
        public final long outdatedTimeInterval;
        public final long outdatedAccuracy;
        public final long minDistanceDelta;

        public Configuration(long outdatedTimeInterval, long outdatedAccuracy, long minDistanceDelta) {
            this.outdatedTimeInterval = outdatedTimeInterval;
            this.outdatedAccuracy = outdatedAccuracy;
            this.minDistanceDelta = minDistanceDelta;
        }
    }

    @NonNull
    private Configuration mConfiguration;

    public LocationDataCache() {
        this(
                new Configuration(OUTDATED_TIME_INTERVAL, OUTDATED_ACCURACY, MIN_DISTANCE_DELTA),
                DEFAULT_CACHE_TTL,
                DEFAULT_CACHE_TTL * 2
        );
    }

    @Override
    protected boolean shouldUpdate(@NonNull Location newData) {
        DebugLogger.info(
            TAG,
            "ShouldUpdateLocation: %s -> %s?: isSavedLocationWorse: %s",
            mCachedData.getData(),
            newData,
            isSavedLocationWorse(newData, mCachedData.getData())
            );
        return WHITE_LIST_PROVIDERS.contains(newData.getProvider()) && (
                mCachedData.isEmpty() ||
                mCachedData.shouldUpdateData() ||
                isSavedLocationWorse(newData, mCachedData.getData())
        );
    }

    private boolean isSavedLocationWorse(final @Nullable Location currentLocation,
                                         final @Nullable Location savedLocation) {
        return isSavedLocationWorse(currentLocation, savedLocation, mConfiguration.outdatedTimeInterval,
                mConfiguration.outdatedAccuracy);
    }

    public static boolean isSavedLocationWorse(@Nullable final Location currentLocation,
                                               @Nullable final Location savedLocation,
                                               final long outdatedTimeInterval,
                                               final long outdatedAccuracy) {
        if (null == savedLocation) {
            // A new location is always better than no location
            return true;
        }

        if (null == currentLocation) {
            // A new location is always better than no location but only if it is valid
            return false;
        }

        // Check whether the new location fix is newer or older
        final long timeDelta = currentLocation.getTime() - savedLocation.getTime();
        final boolean isSignificantlyNewer = timeDelta > outdatedTimeInterval;
        final boolean isSignificantlyOlder = timeDelta < -outdatedTimeInterval;
        final boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            // If it's been more than two minutes since the current location,
            // use the new location because the user has likely moved
            return true;
        } else if (isSignificantlyOlder) {
            // If the new location is more than two minutes older, it must be worse
            return false;
        }

        // Check whether the new location fix is more or less accurate
        final int accuracyDelta = (int) (currentLocation.getAccuracy() - savedLocation.getAccuracy());
        final boolean isLessAccurate = accuracyDelta > 0;
        final boolean isMoreAccurate = accuracyDelta < 0;
        final boolean isSignificantlyLessAccurate = accuracyDelta > outdatedAccuracy;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(currentLocation.getProvider(), savedLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }

        return false;
    }

    static boolean isSameProvider(final @Nullable String provider, final @Nullable String otherProvider) {
        return null == provider ? null == otherProvider : provider.equals(otherProvider);
    }

    @VisibleForTesting
    LocationDataCache(@NonNull Configuration configuration, long refreshTime, long expiryTime) {
        super(refreshTime, expiryTime, "location");
        mConfiguration = configuration;
    }

    @VisibleForTesting
    Configuration getConfiguration() {
        return mConfiguration;
    }
}
