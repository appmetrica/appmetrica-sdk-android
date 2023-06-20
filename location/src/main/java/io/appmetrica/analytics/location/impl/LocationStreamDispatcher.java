package io.appmetrica.analytics.location.impl;

import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.coreapi.internal.annotations.GeoThread;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.coreutils.internal.logger.YLogger;
import io.appmetrica.analytics.locationapi.internal.CacheArguments;
import io.appmetrica.analytics.locationapi.internal.LocationFilter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationStreamDispatcher {

    private static final String TAG = "[LocationStreamDispatcher]";

    @NonNull
    private LocationConfig locationConfig;
    @NonNull
    private final LocationDataCache locationDataCache;
    @NonNull
    private final LocationCacheConsumer locationCacheConsumer;
    @NonNull
    private final List<Consumer<Location>> geoConsumers;
    @NonNull
    private final Map<String, SingleProviderLocationFiltrator> locationHandlers = new HashMap<>();

    public LocationStreamDispatcher(@NonNull List<Consumer<Location>> geoConsumers,
                                    @NonNull LocationConfig locationConfig) {
        YLogger.info(TAG, "Consumers: ", geoConsumers);
        this.geoConsumers = geoConsumers;
        this.locationConfig = locationConfig;
        locationDataCache = new LocationDataCache();
        locationCacheConsumer = new LocationCacheConsumer(locationDataCache);
    }

    @GeoThread
    public void onLocationChanged(@NonNull final Location location) {
        YLogger.info(TAG, "onLocation: %s", location);
        String provider = location.getProvider();
        SingleProviderLocationFiltrator singleProviderLocationFiltrator = locationHandlers.get(provider);
        if (singleProviderLocationFiltrator == null) {
            singleProviderLocationFiltrator =
                createLocationHandler(locationConfig.getLocationFilter());
            locationHandlers.put(provider, singleProviderLocationFiltrator);
        } else {
            YLogger.d(
                "%sLocation handler set location collection config: %s; and handleLocation: %s",
                TAG,
                locationConfig.getLocationFilter(),
                location
            );
            singleProviderLocationFiltrator.setLocationFilter(locationConfig.getLocationFilter());
        }
        singleProviderLocationFiltrator.handleLocation(location);
    }

    @Nullable
    public Location getCachedLocation() {
        return locationDataCache.getData();
    }

    @NonNull
    private SingleProviderLocationFiltrator createLocationHandler(@NonNull LocationFilter locationFilter) {
        SingleProviderLocationFiltrator singleProviderLocationFiltrator =
            new SingleProviderLocationFiltrator(locationFilter);
        singleProviderLocationFiltrator.registerConsumer(locationCacheConsumer);
        for (Consumer<Location> consumer : geoConsumers) {
            singleProviderLocationFiltrator.registerConsumer(consumer);
        }

        return singleProviderLocationFiltrator;
    }

    @GeoThread
    public void setLocationConfig(@NonNull LocationConfig locationConfig) {
        YLogger.d("%supdate collection config %s", TAG, locationConfig);
        this.locationConfig = locationConfig;
        CacheArguments cacheArguments = locationConfig.getCacheArguments();
        locationDataCache.updateCacheControl(
            cacheArguments.getRefreshPeriod(),
            cacheArguments.getOutdatedTimeInterval()
        );
    }

    @NonNull
    public LocationDataCache getLocationDataCache() {
        return locationDataCache;
    }

}
