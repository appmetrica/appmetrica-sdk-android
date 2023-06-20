package io.appmetrica.analytics.gpllibrary.internal;

import android.location.Location;
import android.location.LocationListener;
import android.os.Looper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class GplLibraryWrapperTest {

    @Mock
    private FusedLocationProviderClient mFusedLocationProviderClient;
    @Mock
    private LocationListener mLocationListener;
    @Mock
    private Looper mLooper;
    @Mock
    private Executor mExecutor;
    @Mock
    GplLibraryWrapper.ClientProvider mClientProvider;
    private GplLibraryWrapper mGplLibraryWrapper;
    private final long mInterval = 1234;

    @Before
    public void setUp() throws Throwable {
        MockitoAnnotations.openMocks(this);
        when(mClientProvider.createClient()).thenReturn(mFusedLocationProviderClient);
        mGplLibraryWrapper = new GplLibraryWrapper(mClientProvider, mLocationListener, mLooper, mExecutor, mInterval);
    }

    @Test
    public void testUpdateLastKnown() throws Throwable {
        Task<Location> locationTask = mock(Task.class);
        when(mFusedLocationProviderClient.getLastLocation()).thenReturn(locationTask);
        mGplLibraryWrapper.updateLastKnownLocation();
        ArgumentCaptor<OnSuccessListener> listenerCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(locationTask).addOnSuccessListener(same(mExecutor), listenerCaptor.capture());
        assertThat(listenerCaptor.getValue()).isExactlyInstanceOf(GplOnSuccessListener.class);
    }

    @Test
    public void testStopLocationUpdates() throws Throwable {
        GplLibraryWrapper.Priority priority = GplLibraryWrapper.Priority.PRIORITY_HIGH_ACCURACY;
        mGplLibraryWrapper.startLocationUpdates(priority);
        ArgumentCaptor<LocationCallback> locationCallbackCaptor = ArgumentCaptor.forClass(LocationCallback.class);
        verify(mFusedLocationProviderClient).requestLocationUpdates(
                argThat(new ArgumentMatcher<LocationRequest>() {
                    @Override
                    public boolean matches(LocationRequest argument) {
                        return argument.getInterval() == mInterval  && argument.getPriority() == LocationRequest.PRIORITY_HIGH_ACCURACY;
                    }
                }),
                locationCallbackCaptor.capture(),
                same(mLooper)
        );
        LocationCallback locationCallback = locationCallbackCaptor.getValue();
        assertThat(locationCallback).isExactlyInstanceOf(GplLocationCallback.class);

        mGplLibraryWrapper.stopLocationUpdates();
        verify(mFusedLocationProviderClient).removeLocationUpdates(locationCallback);
    }
}
