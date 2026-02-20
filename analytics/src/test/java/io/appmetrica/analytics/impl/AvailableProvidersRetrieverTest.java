package io.appmetrica.analytics.impl;

import android.location.LocationManager;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class AvailableProvidersRetrieverTest extends CommonTest {

    @Mock
    private LocationManager mLocationManager;
    private AvailableProvidersRetriever mAvailableProvidersRetriever;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mAvailableProvidersRetriever = new AvailableProvidersRetriever(mLocationManager);
    }

    @Test
    public void testGetAvailableProviders() {
        List<String> providers = Arrays.asList("gps", "passive");
        when(mLocationManager.getProviders(true)).thenReturn(providers);
        assertThat(mAvailableProvidersRetriever.getAvailableProviders()).isEqualTo(providers);
    }

    @Test
    public void testGetAvailableProvidersNull() {
        when(mLocationManager.getProviders(true)).thenReturn(null);
        assertThat(mAvailableProvidersRetriever.getAvailableProviders()).isNotNull().isEmpty();
    }

    @Test
    public void getAvailableProvidersException() {
        doThrow(new RuntimeException()).when(mLocationManager).getProviders(true);
        assertThat(mAvailableProvidersRetriever.getAvailableProviders()).isNotNull().isEmpty();
    }

    @Test
    public void testNullLocationManager() {
        assertThat(new AvailableProvidersRetriever((LocationManager) null).getAvailableProviders()).isEmpty();
    }

}
