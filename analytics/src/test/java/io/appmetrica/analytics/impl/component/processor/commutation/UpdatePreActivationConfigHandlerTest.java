package io.appmetrica.analytics.impl.component.processor.commutation;

import android.location.Location;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.GlobalServiceLocator;
import io.appmetrica.analytics.impl.component.CommonArguments;
import io.appmetrica.analytics.impl.component.CommutationComponentId;
import io.appmetrica.analytics.impl.component.CommutationDispatcherComponent;
import io.appmetrica.analytics.impl.component.clients.CommutationClientUnit;
import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UpdatePreActivationConfigHandlerTest extends CommonTest {

    @Rule
    public ContextRule contextRule = new ContextRule();

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Mock
    private CommutationClientUnit mClientUnit;
    @Mock
    private CommutationDispatcherComponent mRegularDispatcherComponent;
    @Mock
    private CounterReport mCounterReport;
    @Mock
    private CounterConfiguration mCounterConfiguration;
    @Mock
    private DataSendingRestrictionControllerImpl mRestrictionController;
    private UpdatePreActivationConfigHandler mHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        doReturn(mRegularDispatcherComponent).when(mClientUnit).getComponent();
        doReturn(new CommutationComponentId(contextRule.getContext().getPackageName())).when(mRegularDispatcherComponent).getComponentId();
        mHandler = new UpdatePreActivationConfigHandler(mRegularDispatcherComponent, mRestrictionController);
    }

    @Test
    public void testRestriction() {
        doReturn(null).when(mCounterConfiguration).getDataSendingEnabled();
        doReturn(new CommonArguments.ReporterArguments(mCounterConfiguration, null)).when(mRegularDispatcherComponent).getConfiguration();
        mHandler.process(mCounterReport, mClientUnit);
        verify(mRestrictionController).setEnabledFromMainReporter(null);
    }

    @Test
    public void advIdentifiersTrackingStatusForNull() {
        when(mCounterConfiguration.isAdvIdentifiersTrackingEnabled()).thenReturn(null);
        doReturn(new CommonArguments.ReporterArguments(mCounterConfiguration, null)).when(mRegularDispatcherComponent).getConfiguration();
        mHandler.process(mCounterReport, mClientUnit);
        verify(GlobalServiceLocator.getInstance().getAdvertisingIdGetter()).updateStateFromClientConfig(true);
    }

    @Test
    public void advIdentifiersTrackingStatusForTrue() {
        when(mCounterConfiguration.isAdvIdentifiersTrackingEnabled()).thenReturn(true);
        doReturn(new CommonArguments.ReporterArguments(mCounterConfiguration, null)).when(mRegularDispatcherComponent).getConfiguration();
        mHandler.process(mCounterReport, mClientUnit);
        verify(GlobalServiceLocator.getInstance().getAdvertisingIdGetter())
            .updateStateFromClientConfig(true);
    }

    @Test
    public void advIdentifiersTrackingStatusForFalse() {
        when(mCounterConfiguration.isAdvIdentifiersTrackingEnabled()).thenReturn(false);
        doReturn(new CommonArguments.ReporterArguments(mCounterConfiguration, null)).when(mRegularDispatcherComponent).getConfiguration();
        mHandler.process(mCounterReport, mClientUnit);
        verify(GlobalServiceLocator.getInstance().getAdvertisingIdGetter())
            .updateStateFromClientConfig(false);
    }

    @Test
    public void updateLocation() {
        Location location = mock(Location.class);
        when(mCounterConfiguration.getManualLocation()).thenReturn(location);
        doReturn(new CommonArguments.ReporterArguments(mCounterConfiguration, null)).when(mRegularDispatcherComponent).getConfiguration();
        mHandler.process(mCounterReport, mClientUnit);
        verify(GlobalServiceLocator.getInstance().getLocationClientApi()).updateLocationFromClient(location);
    }

    @Test
    public void updateLocationForNull() {
        when(mCounterConfiguration.getManualLocation()).thenReturn(null);
        doReturn(new CommonArguments.ReporterArguments(mCounterConfiguration, null)).when(mRegularDispatcherComponent).getConfiguration();
        mHandler.process(mCounterReport, mClientUnit);
        verify(GlobalServiceLocator.getInstance().getLocationClientApi()).updateLocationFromClient(null);
    }
}
