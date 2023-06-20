package io.appmetrica.analytics.location.impl;

import android.location.Location;
import android.location.LocationManager;
import io.appmetrica.analytics.coreutils.internal.cache.DataCache;
import io.appmetrica.analytics.gpllibrary.internal.GplLibraryWrapper;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class LocationDataCacheTests extends DataCacheTest<Location> {

    public static final long DEFAULT_CACHE_TTL = TimeUnit.SECONDS.toMillis(10);

    private Location mLocation;
    private static final String PROVIDER_NAME = LocationManager.GPS_PROVIDER;
    private LocationDataCache.Configuration mDefaultConfiguration;
    private LocationDataCache locationDataCache;
    private final long initialCacheControl = 10000L;

    @Test
    public void testConstructor() {
        locationDataCache = new LocationDataCache();
        assertThat(locationDataCache.getConfiguration()).isNotNull();
        assertThat(locationDataCache.getCachedData().getRefreshTime()).isEqualTo(DEFAULT_CACHE_TTL);
        assertThat(locationDataCache.getCachedData().getExpiryTime()).isEqualTo(DEFAULT_CACHE_TTL * 2);
    }

    @Test
    public void testShouldStoreLocationInCacheIfCachedDataIsNull() {
        assertThat(locationDataCache.shouldUpdate(mLocation)).isTrue();
    }

    @Test
    public void testShouldStoreLocationInCacheIfCachedDataIsExpired() {
        Location previousLocation = new Location(PROVIDER_NAME);
        previousLocation.setLongitude(100f);
        previousLocation.setLatitude(100f);
        previousLocation.setTime(System.currentTimeMillis());
        locationDataCache = new LocationDataCache(mDefaultConfiguration, 0, 0);
        locationDataCache.updateData(previousLocation);
        try {
            Thread.sleep(1);
        } catch (Exception ignored) {
        }
        assertThat(locationDataCache.shouldUpdate(mLocation)).isTrue();
    }

    @Test
    public void testShouldNotStoreNewTheSameLocationInCacheIfCacheDataIsNotExpired() {
        Location previousLocation = new Location(mLocation);
        previousLocation.setProvider(LocationManager.NETWORK_PROVIDER);
        locationDataCache.updateData(previousLocation);
        assertThat(locationDataCache.shouldUpdate(mLocation)).isFalse();
        Location cachedLocation = locationDataCache.getData();
        assertThat(cachedLocation).isEqualToComparingFieldByField(previousLocation);
        assertThat(cachedLocation).isNotEqualTo(mLocation);
    }

    @Test
    public void testShouldStoreNewLocationIfNewLocationMuchNewer() {
        Location previousLocation = new Location(mLocation);
        previousLocation.setProvider(LocationManager.NETWORK_PROVIDER);
        previousLocation.setTime(mLocation.getTime() - TimeUnit.DAYS.toMillis(1));
        locationDataCache.updateData(previousLocation);
        assertThat(locationDataCache.shouldUpdate(mLocation)).isTrue();
    }

    @Test
    public void testShouldNotStoreNewLocationIfNewLocationMuchOlder() {
        Location prevLocation = new Location(mLocation);
        prevLocation.setProvider(LocationManager.NETWORK_PROVIDER);
        mLocation.setTime(mLocation.getTime() - TimeUnit.DAYS.toMillis(1));
        locationDataCache.updateData(prevLocation);
        assertThat(locationDataCache.shouldUpdate(mLocation)).isFalse();
    }

    @Test
    public void testShouldStoreNewLocationIfNewLocationMoreAccuracy() {
        Location previousLocation = new Location(mLocation);
        previousLocation.setProvider(LocationManager.NETWORK_PROVIDER);
        previousLocation.setAccuracy(1000);
        mLocation.setAccuracy(100);
        locationDataCache.updateData(previousLocation);
        assertThat(locationDataCache.shouldUpdate(mLocation)).isTrue();
    }

    @Test
    public void testShouldNotStoreNewLocationIfNewLocationLessAccuracy() {
        Location prevLocation = new Location(mLocation);
        prevLocation.setProvider(LocationManager.NETWORK_PROVIDER);
        prevLocation.setAccuracy(100);
        mLocation.setAccuracy(1000);
        locationDataCache.updateData(prevLocation);
        assertThat(locationDataCache.shouldUpdate(mLocation)).isFalse();
    }

    @Test
    public void testShouldUseNewLocationIfItNewerAndTheSameAccurate() {
        Location prevLocation = new Location(mLocation);
        prevLocation.setProvider(LocationManager.NETWORK_PROVIDER);
        mLocation.setTime(prevLocation.getTime() + 1);
        locationDataCache.updateData(prevLocation);
        assertThat(locationDataCache.shouldUpdate(mLocation)).isTrue();
    }

    @Test
    public void testShouldUseNewLocationIfNewLocationFromTheSameProviderAndLittleNewerAndLessAccurate() {
        Location prevLocation = new Location(mLocation);
        prevLocation.setAccuracy(100);
        mLocation.setTime(prevLocation.getTime() + 1);
        mLocation.setAccuracy(prevLocation.getAccuracy() + 1);
        locationDataCache.updateData(prevLocation);
        assertThat(locationDataCache.shouldUpdate(mLocation)).isTrue();
    }

    @Test
    public void testShouldUpdateDifferentProviders() {
        locationDataCache = new LocationDataCache(mDefaultConfiguration, 0, 0);
        Location gpsLocation = new Location(LocationManager.GPS_PROVIDER);
        assertThat(locationDataCache.shouldUpdate(gpsLocation)).isTrue();
        Location networkLocation = new Location(LocationManager.NETWORK_PROVIDER);
        assertThat(locationDataCache.shouldUpdate(networkLocation)).isTrue();
        Location fusedLocation = new Location(GplLibraryWrapper.FUSED_PROVIDER);
        assertThat(locationDataCache.shouldUpdate(fusedLocation)).isFalse();
    }

    @Override
    protected DataCache<Location> createDataCache(long refreshTime, long expiryTime) {
        mDefaultConfiguration = new LocationDataCache.Configuration(LocationDataCache.OUTDATED_TIME_INTERVAL,
                LocationDataCache.OUTDATED_ACCURACY, LocationDataCache.MIN_DISTANCE_DELTA);
        locationDataCache = new LocationDataCache(mDefaultConfiguration, refreshTime, expiryTime);
        return locationDataCache;
    }

    @Override
    protected DataCache<Location> createDataCache() {
        return new LocationDataCache();
    }

    @Override
    protected long getDefaultCacheRefreshTime() {
        return initialCacheControl;
    }

    @Override
    protected long getDefaultCacheExpiryTime() {
        return initialCacheControl * 2;
    }

    @Override
    protected Location createMockedData() {
        mLocation = new Location(PROVIDER_NAME);
        return mLocation;
    }

    @Override
    protected Location createAnotherData() {
        Location location = new Location(PROVIDER_NAME);
        location.setLongitude(10);
        location.setLatitude(10);
        location.setAccuracy(0);
        location.setTime(System.currentTimeMillis());

        return location;
    }
}
