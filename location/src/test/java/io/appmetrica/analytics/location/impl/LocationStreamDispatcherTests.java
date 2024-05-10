package io.appmetrica.analytics.location.impl;

import android.location.Location;
import android.location.LocationManager;
import io.appmetrica.analytics.coreapi.internal.backport.Consumer;
import io.appmetrica.analytics.locationapi.internal.CacheArguments;
import io.appmetrica.analytics.locationapi.internal.LocationFilter;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class LocationStreamDispatcherTests extends CommonTest {

    @Mock
    private LocationConfig locationConfig;
    @Mock
    private CacheArguments cacheArguments;
    @Mock
    private LocationFilter locationFilter;
    @Mock
    private Consumer<Location> firstConsumer;
    @Mock
    private Consumer<Location> secondConsumers;
    @Mock
    private Location location;
    private LocationStreamDispatcher locationStreamDispatcher;

    private final long refreshPeriod = 23423L;
    private final long expirationTime = 2323211324L;

    private LocationDataCache locationDataCache;
    private LocationCacheConsumer locationCacheConsumer;

    @Rule
    public final MockedConstructionRule<LocationDataCache> locationDataCacheMockedRule =
        new MockedConstructionRule<>(LocationDataCache.class);

    @Rule
    public final MockedConstructionRule<LocationCacheConsumer> locationCacheConsumerMockedRule =
        new MockedConstructionRule<>(LocationCacheConsumer.class);

    @Rule
    public final MockedConstructionRule<SingleProviderLocationFiltrator> singleProviderLocationFiltratorMockedRule =
        new MockedConstructionRule<>(SingleProviderLocationFiltrator.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(locationConfig.getCacheArguments()).thenReturn(cacheArguments);
        when(locationConfig.getLocationFilter()).thenReturn(locationFilter);
        when(cacheArguments.getRefreshPeriod()).thenReturn(refreshPeriod);
        when(cacheArguments.getOutdatedTimeInterval()).thenReturn(expirationTime);

        locationStreamDispatcher = new LocationStreamDispatcher(
            Arrays.asList(firstConsumer, secondConsumers),
            locationConfig
        );
        locationDataCache = locationDataCache();
        locationCacheConsumer = locationCacheConsumer();
    }

    @Test
    public void testOnLocationChangedShouldCreateConsumerOnlyOnce() {
        sendLocationsWithCustomProvider(5, 3);
        locationCacheConsumer();
    }

    private void sendLocationsWithCustomProvider(int providersCount, int locationsPerProviderCount) {
        for (int i = 0; i < providersCount; i++) {
            when(location.getProvider()).thenReturn("Test" + i);
            for (int j = 0; j < locationsPerProviderCount; j++) {
                locationStreamDispatcher.onLocationChanged(location);
            }
        }
    }

    @Test
    public void testOnNetworkLocationChangedShouldCreateNetworkLocationHandler() {
        sendLocationForProvider(LocationManager.NETWORK_PROVIDER, 5);
        SingleProviderLocationFiltrator filtrator = onlyFiltrator();
        verify(filtrator).registerConsumer(locationCacheConsumer);
    }

    @Test
    public void testOnPassiveLocationChangedShouldCreatePassiveLocationHandler() {
        sendLocationForProvider(LocationManager.PASSIVE_PROVIDER, 5);
        SingleProviderLocationFiltrator filtrator = onlyFiltrator();
        verify(filtrator).registerConsumer(locationCacheConsumer);
    }

    @Test
    public void testOnNetworkLocationChangedShouldDispatchLocationToNetworkLocationHandler() {
        sendLocationForProvider(LocationManager.NETWORK_PROVIDER, 1);
        verify(onlyFiltrator(), times(1)).handleLocation(location);
    }

    @Test
    public void testOnNetworkLocationChangedShouldDispatchLocationToNetworkLocationHandlerEveryTime() {
        int count = 10;
        sendRandomLocationForProvider(LocationManager.NETWORK_PROVIDER, count);
        verify(onlyFiltrator(), times(count)).handleLocation(any(Location.class));
    }

    @Test
    public void testOnLocationChangedShouldDispatchLocationToValidProvider() {
        List<Location> locations = new ArrayList<Location>();
        int locationCountPerProvider = 10;
        for (int i = 0; i < locationCountPerProvider; i++) {
            Location networkLocation = mock(Location.class);
            locations.add(networkLocation);
        }
        Collections.shuffle(locations);
        for (Location location : locations) {
            locationStreamDispatcher.onLocationChanged(location);
        }
        verify(onlyFiltrator(), times(locationCountPerProvider)).handleLocation(any(Location.class));
    }

    @Test
    public void testDifferentLocationHandlersForDifferentProviders() {
        when(location.getProvider()).thenReturn(LocationManager.NETWORK_PROVIDER);
        locationStreamDispatcher.onLocationChanged(location);
        onlyFiltrator().handleLocation(location);
        Location passiveProviderLocation = mock(Location.class);
        when(passiveProviderLocation.getProvider()).thenReturn(LocationManager.PASSIVE_PROVIDER);
        locationStreamDispatcher.onLocationChanged(passiveProviderLocation);
        assertThat(singleProviderLocationFiltratorMockedRule.getConstructionMock().constructed()).hasSize(2);
        verify(singleProviderLocationFiltratorMockedRule.getConstructionMock().constructed().get(1))
            .handleLocation(passiveProviderLocation);
    }

    @Test
    public void testGetCachedLocationShouldReturnLocationFromGlobalStateIfExists() {
        Location inputLocation = mock(Location.class);
        when(inputLocation.getProvider()).thenReturn(LocationManager.NETWORK_PROVIDER);
        locationStreamDispatcher.onLocationChanged(inputLocation);
        Location location = mock(Location.class);
        when(locationDataCache.getData()).thenReturn(location);
        assertThat(locationStreamDispatcher.getCachedLocation()).isEqualTo(location);
        verify(locationDataCache, only()).getData();
    }

    @Test
    public void testGetCachedLocationShouldReturnLocationFromGlobalState() {
        Location location = mock(Location.class);
        when(locationDataCache.getData()).thenReturn(this.location);
        locationStreamDispatcher.onLocationChanged(location);
        assertThat(locationStreamDispatcher.getCachedLocation()).isEqualTo(this.location);
    }

    @Test
    public void testNullCachedLocation() {
        when(locationDataCache.getData()).thenReturn(null);
        assertThat(locationStreamDispatcher.getCachedLocation()).isNull();
    }

    @Test
    public void setLocationConfig() {
        locationStreamDispatcher.setLocationConfig(locationConfig);
        verify(locationDataCache).updateCacheControl(refreshPeriod,expirationTime);
    }

    @Test
    public void getLocationDataCache() {
        assertThat(locationStreamDispatcher.getLocationDataCache()).isEqualTo(locationDataCache);
    }

    @Test
    public void getCachedLocation() {
        when(locationDataCache.getData()).thenReturn(location);
        assertThat(locationStreamDispatcher.getCachedLocation()).isEqualTo(location);
    }

    @Test
    public void getCachedLocationForNull() {
        assertThat(locationStreamDispatcher.getCachedLocation()).isNull();
    }

    private void sendLocationForProvider(String provider, int count) {
        for (int i = 0; i < count; i++) {
            when(location.getProvider()).thenReturn(provider);
            locationStreamDispatcher.onLocationChanged(location);
        }
    }

    private void sendRandomLocationForProvider(String provider, int count) {
        for (int i = 0; i < count; i++) {
            Location location = mock(Location.class);
            when(location.getProvider()).thenReturn(provider);
            locationStreamDispatcher.onLocationChanged(location);
        }
    }

    private LocationDataCache locationDataCache() {
        assertThat(locationDataCacheMockedRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(locationDataCacheMockedRule.getArgumentInterceptor().flatArguments()).isEmpty();
        return locationDataCacheMockedRule.getConstructionMock().constructed().get(0);
    }

    private LocationCacheConsumer locationCacheConsumer() {
        assertThat(locationCacheConsumerMockedRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(locationCacheConsumerMockedRule.getArgumentInterceptor().flatArguments())
            .containsExactly(locationDataCache);
        return locationCacheConsumerMockedRule.getConstructionMock().constructed().get(0);
    }

    private SingleProviderLocationFiltrator onlyFiltrator() {
        assertThat(singleProviderLocationFiltratorMockedRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(singleProviderLocationFiltratorMockedRule.getArgumentInterceptor().flatArguments())
            .containsExactly(locationFilter);
        return singleProviderLocationFiltratorMockedRule.getConstructionMock().constructed().get(0);
    }
}
