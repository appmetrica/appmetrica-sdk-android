package io.appmetrica.analytics.gpllibrary.internal;

import android.location.Location;
import android.location.LocationListener;
import android.os.Looper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class GplLibraryWrapperStartLocationUpdatesTest {

    private final FusedLocationProviderClient mFusedLocationProviderClient;
    private final LocationListener mLocationListener;
    private final Looper mRequestUpdatesLooper;
    private final Executor mLastKnownExecutor;
    private final GplLibraryWrapper mGplLibraryWrapper;
    private final long mInterval = 1234;
    private final GplLibraryWrapper.Priority mPriority;
    private final int mExpectedPriority;

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0} to {1}")
    public static Collection<Object[]> getData() {
        return Arrays.asList(
                new Object[]{GplLibraryWrapper.Priority.PRIORITY_NO_POWER, LocationRequest.PRIORITY_NO_POWER},
                new Object[]{GplLibraryWrapper.Priority.PRIORITY_LOW_POWER, LocationRequest.PRIORITY_LOW_POWER},
                new Object[]{GplLibraryWrapper.Priority.PRIORITY_BALANCED_POWER_ACCURACY, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY},
                new Object[]{GplLibraryWrapper.Priority.PRIORITY_HIGH_ACCURACY, LocationRequest.PRIORITY_HIGH_ACCURACY}
        );
    }

    public GplLibraryWrapperStartLocationUpdatesTest(GplLibraryWrapper.Priority priority, int expectedPriority) throws Throwable {
        mPriority = priority;
        mExpectedPriority = expectedPriority;
        GplLibraryWrapper.ClientProvider clientProvider = mock(GplLibraryWrapper.ClientProvider.class);
        mFusedLocationProviderClient = mock(FusedLocationProviderClient.class);
        when(clientProvider.createClient()).thenReturn(mFusedLocationProviderClient);
        mLocationListener = mock(LocationListener.class);
        mRequestUpdatesLooper = mock(Looper.class);
        mLastKnownExecutor = mock(Executor.class);
        mGplLibraryWrapper = new GplLibraryWrapper(clientProvider, mLocationListener, mRequestUpdatesLooper, mLastKnownExecutor, mInterval);
    }

    @Test
    public void testStartLocationUpdates() throws Throwable {
        mGplLibraryWrapper.startLocationUpdates(mPriority);
        ArgumentCaptor<LocationCallback> locationCallbackCaptor = ArgumentCaptor.forClass(LocationCallback.class);
        verify(mFusedLocationProviderClient).requestLocationUpdates(
                argThat(new ArgumentMatcher<LocationRequest>() {
                    @Override
                    public boolean matches(LocationRequest argument) {
                        return argument.getInterval() == mInterval  && argument.getPriority() == mExpectedPriority;
                    }
                }),
                locationCallbackCaptor.capture(),
                same(mRequestUpdatesLooper)
        );
        LocationResult locationResult = mock(LocationResult.class);
        Location location = mock(Location.class);
        when(locationResult.getLastLocation()).thenReturn(location);
        locationCallbackCaptor.getValue().onLocationResult(locationResult);
        verify(mLocationListener).onLocationChanged(location);
    }

}
