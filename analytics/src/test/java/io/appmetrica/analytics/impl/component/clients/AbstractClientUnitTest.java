package io.appmetrica.analytics.impl.component.clients;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.RegularDispatcherComponent;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class AbstractClientUnitTest extends CommonTest {

    private interface ReportProxy {
        void handleReport(@NonNull CounterReport counterReport, @NonNull CommonArguments sdkConfig);
    }

    @Mock
    private ReportProxy mReportProxy;
    @Mock
    private RegularDispatcherComponent mComponent;
    private Context mContext;
    @Mock
    private CounterReport mCounterReport;
    @Mock
    private CommonArguments mClientConfiguration;

    private AbstractClientUnit mAbstractClientUnit;

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        mAbstractClientUnit = new AbstractClientUnit(mContext, mComponent) {
            @Override
            protected void handleReport(@NonNull CounterReport report, @NonNull CommonArguments sdkConfig) {
                mReportProxy.handleReport(report, sdkConfig);
            }
        };
    }

    @Test
    public void testTrackingRegistration() {
        verify(GlobalServiceLocator.getInstance().getLocationClientApi()).registerWakelock(mAbstractClientUnit);
    }

    @Test
    public void testTrackingUnregistration() {
        mAbstractClientUnit.onDisconnect();
        verify(GlobalServiceLocator.getInstance().getLocationClientApi()).removeWakelock(mAbstractClientUnit);
    }

    @Test
    public void testGetContext() {
        assertThat(mAbstractClientUnit.getContext()).isSameAs(RuntimeEnvironment.getApplication());
    }

    @Test
    public void testGetComponent() {
        assertThat(mAbstractClientUnit.getComponentUnit()).isSameAs(mComponent);
    }

    @Test
    public void testClientConnection() {
        verify(mComponent, times(1)).connectClient(mAbstractClientUnit);
    }

    @Test
    public void testClientDisconnection() {
        mAbstractClientUnit.onDisconnect();
        verify(mComponent, times(1)).disconnectClient(mAbstractClientUnit);
    }

    @Test
    public void testDispatchReportToProxy() {
        mAbstractClientUnit.handle(mCounterReport, mClientConfiguration);
        verify(mReportProxy, times(1)).handleReport(mCounterReport, mClientConfiguration);
    }

}
