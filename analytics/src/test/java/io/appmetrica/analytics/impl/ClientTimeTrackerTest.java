package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ClientTimeTrackerTest extends CommonTest {

    @Mock
    private SystemTimeProvider mSystemTimeProvider;

    private ClientTimeTracker mClientTimeTracker;

    private static final long CORE_CREATION_TIME = 21312312;
    private static final long GETTING_TIME_SINCE_ACTION_TIME = 31312312;
    private static final long TIME_SINCE_ACTIVATION = GETTING_TIME_SINCE_ACTION_TIME - CORE_CREATION_TIME;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mClientTimeTracker = new ClientTimeTracker(mSystemTimeProvider);
    }

    @Test
    public void testDefaultConstructor() {
        assertThat(new ClientTimeTracker().getSystemTimeProvider()).isNotNull();
    }

    @Test
    public void testGetTimeSinceCoreCreationWithoutTracking() {
        when(mSystemTimeProvider.elapsedRealtime()).thenReturn(GETTING_TIME_SINCE_ACTION_TIME);
        assertThat(mClientTimeTracker.getTimeSinceCoreCreation()).isNull();
    }

    @Test
    public void testGetTimeSinceCoreCreationIfTimeDoesNotChanged() {
        when(mSystemTimeProvider.elapsedRealtime()).thenReturn(CORE_CREATION_TIME);
        mClientTimeTracker.trackCoreCreation();
        assertThat(mClientTimeTracker.getTimeSinceCoreCreation()).isZero();
    }

    @Test
    public void testGetTimeSinceCoreCreation() {
        when(mSystemTimeProvider.elapsedRealtime()).thenReturn(CORE_CREATION_TIME);
        mClientTimeTracker.trackCoreCreation();
        when(mSystemTimeProvider.elapsedRealtime()).thenReturn(GETTING_TIME_SINCE_ACTION_TIME);
        assertThat(mClientTimeTracker.getTimeSinceCoreCreation()).isEqualTo(TIME_SINCE_ACTIVATION);
    }
}
