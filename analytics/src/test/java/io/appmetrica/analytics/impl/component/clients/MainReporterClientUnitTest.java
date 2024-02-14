package io.appmetrica.analytics.impl.component.clients;

import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommonArgumentsTestUtils;
import io.appmetrica.analytics.impl.component.MainReporterComponentUnit;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
import io.appmetrica.analytics.impl.request.StartupArgumentsTest;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MainReporterClientUnitTest extends CommonTest {

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Mock
    private RegularDispatcherComponent<MainReporterComponentUnit> mComponentUnit;
    private MainReporterClientUnit mMainReporterClientUnit;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        GlobalServiceLocator.getInstance().initAsync();
        mMainReporterClientUnit = new MainReporterClientUnit(
                RuntimeEnvironment.getApplication(),
                mComponentUnit
        );
    }

    @Test
    public void testTrackingStatusUpdatingWithTrue() {
        testTrackingStatusUpdating(true);
    }

    @Test
    public void testTrackingStatusUpdatingWithFalse() {
        testTrackingStatusUpdating(false);
    }

    @Test
    public void testDispatchEvent() {
        CounterReport counterReport =
                new CounterReport("Test event", InternalEvents.EVENT_TYPE_REGULAR.getTypeId());
        CommonArguments mockedArguments = CommonArgumentsTestUtils.createMockedArguments();
        mMainReporterClientUnit.handleReport(counterReport, mockedArguments);
        verify(mComponentUnit, times(1)).handleReport(counterReport, mockedArguments);
    }

    private void testTrackingStatusUpdating(boolean value) {
        mMainReporterClientUnit = new MainReporterClientUnit(RuntimeEnvironment.getApplication(), mComponentUnit);
        CounterConfiguration counterConfiguration = mock(CounterConfiguration.class);
        when(counterConfiguration.isLocationTrackingEnabled()).thenReturn(value);

        CommonArguments clientConfiguration = new CommonArguments(
                StartupArgumentsTest.empty(),
                new CommonArguments.ReporterArguments(counterConfiguration, null),
                null
        );
        mMainReporterClientUnit.handleReport(new CounterReport(), clientConfiguration);
        verify(GlobalServiceLocator.getInstance().getLocationClientApi()).updateTrackingStatusFromClient(value);
    }
}
