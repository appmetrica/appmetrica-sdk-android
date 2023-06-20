package io.appmetrica.analytics.impl.billing;

import android.content.Context;
import io.appmetrica.analytics.billinginterface.internal.BillingType;
import io.appmetrica.analytics.billinginterface.internal.config.BillingConfig;
import io.appmetrica.analytics.billinginterface.internal.monitor.BillingMonitor;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoSender;
import io.appmetrica.analytics.billinginterface.internal.storage.BillingInfoStorage;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateObserver;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateProvider;
import io.appmetrica.analytics.impl.startup.StartupState;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BillingMonitorWrapperTest extends CommonTest {

    @Mock
    private Context context;
    @Mock
    private Executor workingExecutor;
    @Mock
    private Executor uiExecutor;
    private final BillingType billingType = BillingType.LIBRARY_V3;
    @Mock
    private BillingInfoStorage storage;
    @Mock
    private BillingInfoSender sender;
    @Mock
    private ApplicationStateProvider applicationStateProvider;
    @Mock
    private BillingMonitorProvider billingMonitorProvider;
    @Mock
    private BillingMonitor billingMonitor;
    @Mock
    private BillingConfig config;
    @Captor
    private ArgumentCaptor<ApplicationStateObserver> observerCaptor;
    private StartupState startupState;
    private BillingMonitorWrapper wrapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        startupState = TestUtils.createDefaultStartupStateBuilder()
                .withAutoInappCollectingConfig(config)
                .build();
        when(billingMonitorProvider.get(context, workingExecutor, uiExecutor, billingType, storage, sender))
                .thenReturn(billingMonitor);
        when(applicationStateProvider.registerStickyObserver(any(ApplicationStateObserver.class)))
                .thenReturn(ApplicationState.UNKNOWN);
        wrapper = new BillingMonitorWrapper(
                context,
                workingExecutor,
                uiExecutor,
                billingType,
                storage,
                sender,
                applicationStateProvider,
                billingMonitorProvider
        );
    }

    @Test
    public void startWatchingFeatureDisabled() {
        wrapper.maybeStartWatching(startupState, false);
        verifyZeroInteractions(billingMonitorProvider);
        verifyZeroInteractions(applicationStateProvider);
    }

    @Test
    public void startWatchingFeatureNull() {
        wrapper.maybeStartWatching(startupState, null);
        verify(billingMonitorProvider).get(context, workingExecutor, uiExecutor, billingType, storage, sender);
        verify(billingMonitor).onBillingConfigChanged(config);
        verify(applicationStateProvider).registerStickyObserver(any(ApplicationStateObserver.class));
    }

    @Test
    public void startWatchingFeatureEnabled() {
        wrapper.maybeStartWatching(startupState, true);
        verify(billingMonitorProvider).get(context, workingExecutor, uiExecutor, billingType, storage, sender);
        verify(billingMonitor).onBillingConfigChanged(config);
        verify(applicationStateProvider).registerStickyObserver(any(ApplicationStateObserver.class));
    }

    @Test
    public void onStartupChangedHasMonitor() {
        wrapper.maybeStartWatching(startupState, null);
        BillingConfig newConfig = mock(BillingConfig.class);
        wrapper.onStartupStateChanged(TestUtils.createDefaultStartupStateBuilder()
                .withAutoInappCollectingConfig(newConfig)
                .build());
        verify(billingMonitor).onBillingConfigChanged(newConfig);
    }

    @Test
    public void onStartupChangedNoMonitor() {
        wrapper.maybeStartWatching(startupState, false);
        BillingConfig newConfig = mock(BillingConfig.class);
        wrapper.onStartupStateChanged(TestUtils.createDefaultStartupStateBuilder()
                .withAutoInappCollectingConfig(newConfig)
                .build());
    }

    @Test
    public void stateIsImmediatelyUnknownHasMonitor() throws Throwable {
        when(applicationStateProvider.registerStickyObserver(any(ApplicationStateObserver.class)))
                .thenReturn(ApplicationState.UNKNOWN);
        wrapper.maybeStartWatching(startupState, true);
        verify(billingMonitor, never()).onSessionResumed();
    }

    @Test
    public void stateIsImmediatelyVisibleHasMonitor() throws Throwable {
        when(applicationStateProvider.registerStickyObserver(any(ApplicationStateObserver.class)))
                .thenReturn(ApplicationState.VISIBLE);
        wrapper.maybeStartWatching(startupState, true);
        verify(billingMonitor).onSessionResumed();
    }

    @Test
    public void stateIsImmediatelyBackgroundHasMonitor() throws Throwable {
        when(applicationStateProvider.registerStickyObserver(any(ApplicationStateObserver.class)))
                .thenReturn(ApplicationState.BACKGROUND);
        wrapper.maybeStartWatching(startupState, true);
        verify(billingMonitor, never()).onSessionResumed();
    }

    @Test
    public void stateIsImmediatelyUnknownNoMonitor() {
        when(applicationStateProvider.registerStickyObserver(any(ApplicationStateObserver.class)))
                .thenReturn(ApplicationState.UNKNOWN);
        wrapper.maybeStartWatching(startupState, false);
    }

    @Test
    public void stateIsImmediatelyVisibleNoMonitor() {
        when(applicationStateProvider.registerStickyObserver(any(ApplicationStateObserver.class)))
                .thenReturn(ApplicationState.VISIBLE);
        wrapper.maybeStartWatching(startupState, false);
    }

    @Test
    public void stateIsImmediatelyBackgroundNoMonitor() {
        when(applicationStateProvider.registerStickyObserver(any(ApplicationStateObserver.class)))
                .thenReturn(ApplicationState.BACKGROUND);
        wrapper.maybeStartWatching(startupState, false);
    }

    @Test
    public void stateChangedToUnknownHasMonitor() throws Throwable {
        wrapper.maybeStartWatching(startupState, true);
        verify(applicationStateProvider).registerStickyObserver(observerCaptor.capture());
        observerCaptor.getValue().onApplicationStateChanged(ApplicationState.UNKNOWN);
        verify(billingMonitor, never()).onSessionResumed();
    }

    @Test
    public void stateChangedToVisibleHasMonitor() throws Throwable {
        wrapper.maybeStartWatching(startupState, true);
        verify(applicationStateProvider).registerStickyObserver(observerCaptor.capture());
        observerCaptor.getValue().onApplicationStateChanged(ApplicationState.VISIBLE);
        verify(billingMonitor).onSessionResumed();
    }

    @Test
    public void stateChangedToBackgroundHasMonitor() throws Throwable {
        wrapper.maybeStartWatching(startupState, true);
        verify(applicationStateProvider).registerStickyObserver(observerCaptor.capture());
        observerCaptor.getValue().onApplicationStateChanged(ApplicationState.BACKGROUND);
        verify(billingMonitor, never()).onSessionResumed();
    }

}
