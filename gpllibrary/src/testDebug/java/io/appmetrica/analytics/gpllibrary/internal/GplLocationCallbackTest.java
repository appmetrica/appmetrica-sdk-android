package io.appmetrica.analytics.gpllibrary.internal;

import android.location.Location;
import android.location.LocationListener;
import com.google.android.gms.location.LocationResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class GplLocationCallbackTest {

    @Mock
    private LocationListener mLocationListener;
    private GplLocationCallback mGplLocationCallback;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mGplLocationCallback = new GplLocationCallback(mLocationListener);
    }

    @Test
    public void testOnLocationResult() {
        Location location = mock(Location.class);
        LocationResult locationResult = mock(LocationResult.class);
        when(locationResult.getLastLocation()).thenReturn(location);
        mGplLocationCallback.onLocationResult(locationResult);
        verify(mLocationListener).onLocationChanged(location);
    }
}
