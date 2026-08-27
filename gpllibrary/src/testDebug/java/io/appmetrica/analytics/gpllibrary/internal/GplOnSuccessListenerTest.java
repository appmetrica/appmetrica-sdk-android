package io.appmetrica.analytics.gpllibrary.internal;

import android.location.Location;
import android.location.LocationListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class GplOnSuccessListenerTest {

    @Mock
    private LocationListener mLocationListener;
    private GplOnSuccessListener mGplOnSuccessListener;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mGplOnSuccessListener = new GplOnSuccessListener(mLocationListener);
    }

    @Test
    public void testOnSuccess() {
        Location location = mock(Location.class);
        mGplOnSuccessListener.onSuccess(location);
        verify(mLocationListener).onLocationChanged(location);
    }
}
