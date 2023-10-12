package io.appmetrica.analytics.location.impl;

import android.location.Location;
import android.os.Bundle;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class LocationListenerWrapperTest extends CommonTest {

    @Mock
    private LocationStreamDispatcher mLocationStreamDispatcher;
    private LocationListenerWrapper mLocationListenerWrapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mLocationListenerWrapper = new LocationListenerWrapper(mLocationStreamDispatcher);
    }

    @Test
    public void testOnLocationChangedNull() {
        mLocationListenerWrapper.onLocationChanged((Location) null);
        verifyNoMoreInteractions(mLocationStreamDispatcher);
    }

    @Test
    public void testLocationChanged() {
        Location location = mock(Location.class);
        mLocationListenerWrapper.onLocationChanged(location);
        verify(mLocationStreamDispatcher).onLocationChanged(location);
    }

    @Test
    public void testOnStatusChanged() {
        mLocationListenerWrapper.onStatusChanged("gps", 0, mock(Bundle.class));
        verifyNoMoreInteractions(mLocationStreamDispatcher);
    }

    @Test
    public void testOnProviderEnabled() {
        mLocationListenerWrapper.onProviderEnabled("network");
        verifyNoMoreInteractions(mLocationStreamDispatcher);
    }

    @Test
    public void testOnProviderDisabled() {
        mLocationListenerWrapper.onProviderDisabled("network");
        verifyNoMoreInteractions(mLocationStreamDispatcher);
    }
}
