package io.appmetrica.analytics.location.impl

import android.location.Location
import android.location.LocationManager
import io.appmetrica.analytics.coreutils.internal.cache.DataCache
import io.appmetrica.analytics.gpllibrary.internal.GplLibraryWrapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class LocationDataCacheTests : DataCacheTest<Location>() {

    private lateinit var location: Location
    private lateinit var defaultConfiguration: LocationDataCache.Configuration
    private lateinit var locationDataCache: LocationDataCache

    @Test
    fun constructor() {
        locationDataCache = LocationDataCache()
        assertThat(locationDataCache.configuration).isNotNull
    }

    @Test
    fun shouldStoreLocationInCacheIfCachedDataIsNull() {
        assertThat(locationDataCache.shouldUpdate(location)).isTrue
    }

    @Test
    fun shouldStoreLocationInCacheIfCachedDataIsExpired() {
        locationDataCache = LocationDataCache(defaultConfiguration, 0, 0)

        // Mock CachedData to indicate cache is expired
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(true)

        val previousLocation = Location(PROVIDER_NAME).apply {
            longitude = 100.0
            latitude = 100.0
            time = System.currentTimeMillis()
        }
        locationDataCache.updateData(previousLocation)

        assertThat(locationDataCache.shouldUpdate(location)).isTrue
    }

    @Test
    fun shouldNotStoreNewTheSameLocationInCacheIfCacheDataIsNotExpired() {
        val previousLocation = Location(location).apply {
            provider = LocationManager.NETWORK_PROVIDER
        }

        // Mock CachedData to return previousLocation
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(false)
        whenever(cachedData().data).thenReturn(previousLocation)
        whenever(cachedData().shouldClearData()).thenReturn(false)

        locationDataCache.updateData(previousLocation)
        assertThat(locationDataCache.shouldUpdate(location)).isFalse
        val cachedLocation = locationDataCache.data
        assertThat(cachedLocation).isEqualToComparingFieldByField(previousLocation)
        assertThat(cachedLocation).isNotEqualTo(location)
    }

    @Test
    fun shouldStoreNewLocationIfNewLocationMuchNewer() {
        val previousLocation = Location(location).apply {
            provider = LocationManager.NETWORK_PROVIDER
            time = location.time - TimeUnit.DAYS.toMillis(1)
        }

        // Mock CachedData to return previousLocation
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(false)
        whenever(cachedData().data).thenReturn(previousLocation)

        locationDataCache.updateData(previousLocation)
        assertThat(locationDataCache.shouldUpdate(location)).isTrue
    }

    @Test
    fun shouldNotStoreNewLocationIfNewLocationMuchOlder() {
        val prevLocation = Location(location).apply {
            provider = LocationManager.NETWORK_PROVIDER
        }
        location.time = location.time - TimeUnit.DAYS.toMillis(1)

        // Mock CachedData to return prevLocation
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(false)
        whenever(cachedData().data).thenReturn(prevLocation)

        locationDataCache.updateData(prevLocation)
        assertThat(locationDataCache.shouldUpdate(location)).isFalse
    }

    @Test
    fun shouldStoreNewLocationIfNewLocationMoreAccuracy() {
        val previousLocation = Location(location).apply {
            provider = LocationManager.NETWORK_PROVIDER
            accuracy = 1000f
        }
        location.accuracy = 100f

        // Mock CachedData to return previousLocation
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(false)
        whenever(cachedData().data).thenReturn(previousLocation)

        locationDataCache.updateData(previousLocation)
        assertThat(locationDataCache.shouldUpdate(location)).isTrue
    }

    @Test
    fun shouldNotStoreNewLocationIfNewLocationLessAccuracy() {
        val prevLocation = Location(location).apply {
            provider = LocationManager.NETWORK_PROVIDER
            accuracy = 100f
        }
        location.accuracy = 1000f

        // Mock CachedData to return prevLocation
        whenever(cachedData().isEmpty).thenReturn(false)
        whenever(cachedData().shouldUpdateData()).thenReturn(false)
        whenever(cachedData().data).thenReturn(prevLocation)

        locationDataCache.updateData(prevLocation)
        assertThat(locationDataCache.shouldUpdate(location)).isFalse
    }

    @Test
    fun shouldUseNewLocationIfItNewerAndTheSameAccurate() {
        val prevLocation = Location(location).apply {
            provider = LocationManager.NETWORK_PROVIDER
        }
        location.time = prevLocation.time + 1
        locationDataCache.updateData(prevLocation)
        assertThat(locationDataCache.shouldUpdate(location)).isTrue
    }

    @Test
    fun shouldUseNewLocationIfNewLocationFromTheSameProviderAndLittleNewerAndLessAccurate() {
        val prevLocation = Location(location).apply {
            accuracy = 100f
        }
        location.time = prevLocation.time + 1
        location.accuracy = prevLocation.accuracy + 1
        locationDataCache.updateData(prevLocation)
        assertThat(locationDataCache.shouldUpdate(location)).isTrue
    }

    @Test
    fun shouldUpdateDifferentProviders() {
        locationDataCache = LocationDataCache(defaultConfiguration, 0, 0)

        // Mock CachedData to indicate cache is not empty
        whenever(cachedData().isEmpty).thenReturn(false)

        val gpsLocation = Location(LocationManager.GPS_PROVIDER)
        assertThat(locationDataCache.shouldUpdate(gpsLocation)).isTrue
        val networkLocation = Location(LocationManager.NETWORK_PROVIDER)
        assertThat(locationDataCache.shouldUpdate(networkLocation)).isTrue
        val fusedLocation = Location(GplLibraryWrapper.FUSED_PROVIDER)
        assertThat(locationDataCache.shouldUpdate(fusedLocation)).isFalse
    }

    @Test
    fun isSavedLocationWorseWithNullSavedLocation() {
        val currentLocation = Location(PROVIDER_NAME)
        assertThat(LocationDataCache.isSavedLocationWorse(currentLocation, null, 1000, 100)).isTrue
    }

    @Test
    fun isSavedLocationWorseWithNullCurrentLocation() {
        val savedLocation = Location(PROVIDER_NAME)
        assertThat(LocationDataCache.isSavedLocationWorse(null, savedLocation, 1000, 100)).isFalse
    }

    @Test
    fun isSavedLocationWorseWithBothNull() {
        // When savedLocation is null, method returns true immediately (before checking currentLocation)
        // This means "no saved location is worse than any current location (even null)"
        assertThat(LocationDataCache.isSavedLocationWorse(null, null, 1000, 100)).isTrue
    }

    @Test
    fun isSameProviderBothNull() {
        assertThat(LocationDataCache.isSameProvider(null, null)).isTrue
    }

    @Test
    fun isSameProviderOneNull() {
        assertThat(LocationDataCache.isSameProvider("gps", null)).isFalse
        assertThat(LocationDataCache.isSameProvider(null, "network")).isFalse
    }

    @Test
    fun isSameProviderSame() {
        assertThat(LocationDataCache.isSameProvider("gps", "gps")).isTrue
    }

    @Test
    fun isSameProviderDifferent() {
        assertThat(LocationDataCache.isSameProvider("gps", "network")).isFalse
    }

    @Test
    fun configurationValues() {
        val config = LocationDataCache.Configuration(1000, 200, 50)
        assertThat(config.outdatedTimeInterval).isEqualTo(1000)
        assertThat(config.outdatedAccuracy).isEqualTo(200)
        assertThat(config.minDistanceDelta).isEqualTo(50)
    }

    override fun createDataCache(refreshTime: Long, expiryTime: Long): DataCache<Location> {
        defaultConfiguration = LocationDataCache.Configuration(
            LocationDataCache.OUTDATED_TIME_INTERVAL,
            LocationDataCache.OUTDATED_ACCURACY,
            LocationDataCache.MIN_DISTANCE_DELTA
        )
        locationDataCache = LocationDataCache(defaultConfiguration, refreshTime, expiryTime)
        return locationDataCache
    }

    override fun createMockedData(): Location {
        location = Location(PROVIDER_NAME)
        return location
    }

    override fun createAnotherData(): Location {
        return Location(PROVIDER_NAME).apply {
            longitude = 10.0
            latitude = 10.0
            accuracy = 0f
            time = System.currentTimeMillis()
        }
    }

    companion object {
        const val DEFAULT_CACHE_TTL = 10_000L // TimeUnit.SECONDS.toMillis(10)
        const val PROVIDER_NAME = LocationManager.GPS_PROVIDER
    }
}
