package io.appmetrica.analytics.impl.component;

import android.content.Context;
import io.appmetrica.analytics.impl.CounterReport;
import io.appmetrica.analytics.impl.InternalEvents;
import io.appmetrica.analytics.impl.component.clients.ClientUnit;
import io.appmetrica.analytics.impl.component.clients.ComponentUnitFactory;
import io.appmetrica.analytics.impl.request.StartupArgumentsTest;
import io.appmetrica.analytics.impl.request.StartupRequestConfig;
import io.appmetrica.analytics.impl.startup.StartupCenter;
import io.appmetrica.analytics.impl.startup.StartupError;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.impl.startup.StartupUnit;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class RegularDispatcherComponentTest extends CommonTest {

    @Mock
    private ComponentId mComponentId;
    @Mock
    private ComponentUnitFactory<ComponentUnit> mComponentUnitFactory;
    private StartupRequestConfig.Arguments startupArguments = StartupArgumentsTest.empty();
    private CommonArguments.ReporterArguments reportArguments = CommonArgumentsTestUtils.emptyReporterArguments();
    private CommonArguments mClientConfiguration;
    @Mock
    private ComponentUnit mReportingComponent;
    @Mock
    private ComponentLifecycleManager<ClientUnit> mLifecycleManager;
    @Mock
    private StartupUnit startupUnit;
    @Mock
    private ReporterArgumentsHolder mReporterArgumentsHolder;
    @Mock
    private StartupCenter mStartupCenter;

    private Context mContext;
    private RegularDispatcherComponent<ComponentUnit> mRegularDispatcherComponent;

    @Rule
    public GlobalServiceLocatorRule mRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();

        when(mReporterArgumentsHolder.getArguments()).thenReturn(reportArguments);
        when(mComponentUnitFactory.createComponentUnit(same(mContext), same(mComponentId), (CommonArguments.ReporterArguments) any(), any(StartupUnit.class)))
            .thenReturn(mReportingComponent);
        mClientConfiguration = new CommonArguments(startupArguments, reportArguments, null);
        doReturn(startupUnit).when(mStartupCenter).getOrCreateStartupUnit(any(Context.class), any(ComponentId.class), any(StartupRequestConfig.Arguments.class));
        mRegularDispatcherComponent = new RegularDispatcherComponent<ComponentUnit>(
            mContext,
            mComponentId,
            mClientConfiguration,
            mReporterArgumentsHolder,
            mComponentUnitFactory,
            mLifecycleManager,
            mStartupCenter
        );
    }

    @Test
    public void testListenerRegistered() {
        verify(mStartupCenter).registerStartupListener(mComponentId, mRegularDispatcherComponent);
    }

    @Test
    public void testHandleReportCreateReportingComponentOnce() {
        CounterReport activationReport = new CounterReport();
        activationReport.setType(InternalEvents.EVENT_TYPE_ACTIVATION.getTypeId());
        CounterReport regularReport = new CounterReport();
        regularReport.setType(InternalEvents.EVENT_TYPE_REGULAR.getTypeId());
        CounterReport startReport = new CounterReport();
        startReport.setType(InternalEvents.EVENT_TYPE_START.getTypeId());

        mRegularDispatcherComponent.handleReport(activationReport, CommonArgumentsTestUtils.createMockedArguments());
        mRegularDispatcherComponent.handleReport(regularReport, CommonArgumentsTestUtils.createMockedArguments());
        mRegularDispatcherComponent.handleReport(startReport, CommonArgumentsTestUtils.createMockedArguments());

        verify(mComponentUnitFactory, times(1)).createComponentUnit(
            mContext,
            mComponentId,
            reportArguments,
            startupUnit
        );
    }

    @Test
    public void testUpdateSdkConfig() {
        CommonArguments clientConfiguration = CommonArgumentsTestUtils.createMockedArguments();
        mRegularDispatcherComponent.handleReport(mock(CounterReport.class), clientConfiguration);
        verify(mReporterArgumentsHolder).updateArguments(clientConfiguration.componentArguments);
    }

    @Test
    public void testUpdateSdkConfigDoNothingIfComponentDidNotCreate() {
        mRegularDispatcherComponent.updateSdkConfig(reportArguments);
        verifyNoMoreInteractions(mReportingComponent);
    }

    @Test
    public void testUpdateSdkConfigUpdateReportingComponent() {
        CounterReport counterReport = new CounterReport();
        counterReport.setType(InternalEvents.EVENT_TYPE_REGULAR.getTypeId());
        CommonArguments.ReporterArguments componentArguments = CommonArgumentsTestUtils.emptyReporterArguments();
        CommonArguments arguments = new CommonArguments(null, componentArguments, null);
        mRegularDispatcherComponent.handleReport(counterReport, arguments);
        verify(mReportingComponent, times(1)).updateSdkConfig(componentArguments);
    }

    @Test
    public void testConnectClientDispatchClientToLifecycleManager() {
        ClientUnit clientUnit = mock(ClientUnit.class);
        mRegularDispatcherComponent.connectClient(clientUnit);
        verify(mLifecycleManager, times(1)).connectClient(clientUnit);
    }

    @Test
    public void testDisconnectClientDispatchClientToLifecycleManager() {
        ClientUnit clientUnit = mock(ClientUnit.class);
        mRegularDispatcherComponent.disconnectClient(clientUnit);
        verify(mLifecycleManager, times(1)).disconnectClient(clientUnit);
    }

    @Test
    public void testStartupChanged() {
        warmUpAllComponents();

        StartupState newState = mock(StartupState.class);
        mRegularDispatcherComponent.onStartupChanged(newState);
        verify(mReportingComponent).onStartupChanged(newState);
    }

    @Test
    public void testStartupError() {
        warmUpAllComponents();

        StartupError error = StartupError.UNKNOWN;
        StartupState startupState = mock(StartupState.class);
        mRegularDispatcherComponent.onStartupError(error, startupState);
        verify(mReportingComponent).onStartupError(error, startupState);
    }

    @Test
    public void testUpdateConfig() {
        CommonArguments.ReporterArguments reporterArguments = mock(CommonArguments.ReporterArguments.class);
        StartupRequestConfig.Arguments startupArguments = mock(StartupRequestConfig.Arguments.class);
        CommonArguments arguments = new CommonArguments(startupArguments, reporterArguments, null);
        mRegularDispatcherComponent.updateConfig(arguments);
        verify(startupUnit).updateConfiguration(startupArguments);
        verify(mReporterArgumentsHolder).updateArguments(reporterArguments);
    }

    private void warmUpAllComponents() {
        CounterReport regularReport = new CounterReport();
        regularReport.setType(InternalEvents.EVENT_TYPE_REGULAR.getTypeId());
        mRegularDispatcherComponent.handleReport(
            regularReport,
            CommonArgumentsTestUtils.createMockedArguments()
        );
    }
}
