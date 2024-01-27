package io.appmetrica.analytics.impl.component;

import io.appmetrica.analytics.internal.CounterConfiguration;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateObserver;
import io.appmetrica.analytics.impl.ApplicationStateProviderImpl;
import io.appmetrica.analytics.impl.CounterConfigurationReporterType;
import io.appmetrica.analytics.impl.DataSendingRestrictionControllerImpl;
import io.appmetrica.analytics.impl.billing.BillingMonitorWrapper;
import io.appmetrica.analytics.impl.component.processor.factory.HandlersFactory;
import io.appmetrica.analytics.impl.component.processor.factory.RegularMainReporterFactory;
import io.appmetrica.analytics.impl.referrer.service.ReferrerHolder;
import io.appmetrica.analytics.impl.referrer.service.ReferrerListenerNotifier;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.impl.InternalEvents.EVENT_TYPE_REGULAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MainReporterComponentUnitTest extends ComponentUnitBaseTest {

    @Mock
    private CounterConfiguration mCounterConfiguration;
    @Mock
    private ReferrerHolder mReferrerHolder;
    @Mock
    private ReferrerListenerNotifier mListener;
    @Mock
    private MainReporterComponentUnitFieldsFactory mFieldsFactory;
    @Mock
    private DataSendingRestrictionControllerImpl dataSendingRestrictionController;
    @Mock
    private ApplicationStateProviderImpl applicationStateProvider;
    @Mock
    private BillingMonitorWrapper billingMonitorWrapper;
    @Mock
    private StartupState startupState;
    private MainReporterComponentUnit mMainReporterComponentUnit;
    private CommonArguments.ReporterArguments sdkConfig = new CommonArguments.ReporterArguments(
            null, null, null, null, null, null, null, null, null, null, null, null, false
    );

    @Rule
    public final GlobalServiceLocatorRule mGlobalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() {
        super.init();
        mMainReporterComponentUnit = (MainReporterComponentUnit) mComponentUnit;
    }

    @Override
    protected void initCustomFields() {
        when(mFieldsFactory.createReferrerListener(any(MainReporterComponentUnit.class))).thenReturn(mListener);
        when(mFieldsFactory.createBillingMonitorWrapper(any(MainReporterComponentUnit.class))).thenReturn(billingMonitorWrapper);
        when(applicationStateProvider.registerStickyObserver(any(ApplicationStateObserver.class)))
                .thenReturn(ApplicationState.UNKNOWN);
    }

    @Override
    protected ComponentUnit createComponentUnit() {
        return new MainReporterComponentUnit(
            mContext,
            mComponentId,
            startupState,
            sdkConfig,
            mAppEnvironmentProvider,
            mTimePassedChecker,
            mFieldsFactory,
            mReferrerHolder,
            dataSendingRestrictionController
        );
    }

    @Override
    protected ComponentUnitFieldsFactory createFieldsFactory() {
        return mFieldsFactory;
    }

    @Test
    public void shouldStartWatching() {
        verify(billingMonitorWrapper).maybeStartWatching(startupState, false);
    }

    @Test
    public void testOnStartupChanged() {
        StartupState startupState = mock(StartupState.class);
        mMainReporterComponentUnit.onStartupChanged(startupState);
        verify(billingMonitorWrapper).onStartupStateChanged(startupState);
    }

    @Test
    public void testChangeRegularEventFactory() {
        ArgumentCaptor<HandlersFactory> captor = ArgumentCaptor.forClass(HandlersFactory.class);
        verify(mEventProcessingStrategyFactory).getHandlersProvider();
        verify(mEventProcessingStrategyFactory).mutateHandlers(eq(EVENT_TYPE_REGULAR), captor.capture());

        assertThat(captor.getValue()).isExactlyInstanceOf(RegularMainReporterFactory.class);
    }

    @Test
    public void testReferrerHolderNotSubscribed() {
        verify(mFieldsFactory).createReferrerListener(mMainReporterComponentUnit);
        verify(mReferrerHolder, never()).subscribe(mListener);
    }

    @Test
    public void testUpdateSdkConfigNullDataSendingEnabled() {
        doReturn(null).when(mCounterConfiguration).getDataSendingEnabled();
        mMainReporterComponentUnit.updateSdkConfig(new CommonArguments.ReporterArguments(mCounterConfiguration, null));
        verify(dataSendingRestrictionController).setEnabledFromMainReporter(null);
    }

    @Test
    public void testUpdateSdkConfigNonNullDataSendingEnabled() {
        doReturn(true).when(mCounterConfiguration).getDataSendingEnabled();
        mMainReporterComponentUnit.updateSdkConfig(new CommonArguments.ReporterArguments(mCounterConfiguration, null));
        verify(dataSendingRestrictionController).setEnabledFromMainReporter(true);
    }

    @Test
    public void testWasReferrerHandled() {
        boolean wasHandled = new Random().nextBoolean();
        when(vitalComponentDataProvider.getReferrerHandled()).thenReturn(wasHandled);
        assertThat(mMainReporterComponentUnit.wasReferrerHandled()).isEqualTo(wasHandled);
    }

    @Test
    public void testOnReferrerHandled() {
        mMainReporterComponentUnit.onReferrerHandled();
        verify(vitalComponentDataProvider).setReferrerHandled(true);
    }

    @Override
    @Test
    public void testGetReporterType() {
        assertThat(mComponentUnit.getReporterType()).isEqualTo(CounterConfigurationReporterType.MAIN);
    }

    @Test
    public void testSubscribeForReferrer() {
        mMainReporterComponentUnit.subscribeForReferrer();
        verify(mReferrerHolder).subscribe(mListener);
    }
}
