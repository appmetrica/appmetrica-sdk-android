package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ExternalAttribution;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.crash.jvm.client.MainReporterAnrController;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.MockProvider;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MainReporterTest extends BaseReporterTest {

    @Mock
    private MainReporterComponents mainReporterComponents;
    @Mock
    private IReporterExtended mAppmetricaReporter;
    @Mock
    private IReporterExtended mPushReporter;
    @Mock
    private UnhandledSituationReporterProvider mAppmetricaReporterProvider;
    @Mock
    private UnhandledSituationReporterProvider mPushReporterProvider;
    @Captor
    private ArgumentCaptor<AppStatusMonitor.Observer> mObserverArgumentCaptor;
    @Mock
    private ReporterEnvironment mAnotherReporterEnvironment;
    @Mock
    private ActivityStateManager activityStateManager;
    @Mock
    private Activity activity;
    @Mock
    private AppMetricaConfigExtension configExtension;

    private IHandlerExecutor executor = MockProvider.mockedBlockingExecutorMock();

    @Rule
    public final ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

    @Rule
    public MockedConstructionRule<MainReporterAnrController> mainReporterAnrControllerRule =
        new MockedConstructionRule<>(MainReporterAnrController.class);

    private MainReporter mMainReporter;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        when(mAnotherReporterEnvironment.getProcessConfiguration()).thenReturn(mProcessConfiguration);
        when(mAnotherReporterEnvironment.getReporterConfiguration()).thenReturn(mCounterConfiguration);
        when(mAppmetricaReporterProvider.getReporter()).thenReturn(mAppmetricaReporter);
        when(mPushReporterProvider.getReporter()).thenReturn(mPushReporter);
        when(ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor())
            .thenReturn(executor);
    }

    @Test
    public void callModulesOnActivatedOnStart() {
        mMainReporter = getReporter();
        mMainReporter.start();

        verify(clientServiceLocatorRule.modulesController).onActivated();
    }

    @Test
    public void userProfileIDIfNotSet() {
        mMainReporter = new MainReporter(mainReporterComponents);
        assertThat(mMainReporter.getEnvironment().getInitialUserProfileID()).isNull();
    }

    @Test
    public void testNativeCrashReportingEnabled() {
        Mockito.reset(nativeCrashClient);
        mConfig = AppMetricaConfig.newConfigBuilder(apiKey).withNativeCrashReporting(true).build();
        String errorEnv = "erroorrenen";
        doReturn(errorEnv).when(mReporterEnvironment).getErrorEnvironment();
        mMainReporter = getReporter();
        mMainReporter.updateConfig(mConfig, configExtension);
        verify(nativeCrashClient).initHandling(eq(mContext), eq(apiKey), eq(errorEnv));
    }

    @Test
    public void testNativeCrashReportingNotSet() {
        Mockito.reset(nativeCrashClient);
        mConfig = AppMetricaConfig.newConfigBuilder(apiKey).build();
        mMainReporter = getReporter();
        mMainReporter.updateConfig(mConfig, configExtension);
        verify(nativeCrashClient).initHandling(eq(mContext), eq(apiKey), eq(null));
    }

    @Test
    public void testNativeCrashReportingDisabled() {
        mConfig = AppMetricaConfig.newConfigBuilder(apiKey).withNativeCrashReporting(false).build();
        mMainReporter = getReporter();
        mMainReporter.updateConfig(mConfig, configExtension);
        verify(nativeCrashClient, never()).initHandling(any(Context.class), any(String.class), any(String.class));
    }

    @Test
    public void testPutErrorEnvironmentShouldUpdateEnvInNativeCrashClient() {
        String errorEnv = "erroorrenen";
        doReturn(errorEnv).when(mReporterEnvironment).getErrorEnvironment();
        mMainReporter = getReporter();
        mMainReporter.putErrorEnvironmentValue("key", "value");
        verify(nativeCrashClient).updateErrorEnvironment(eq(errorEnv));
    }

    @Test
    public void testRegisterObserverFromConstructor() {
        when(mReporterEnvironment.getProcessConfiguration()).thenReturn(mProcessConfiguration);
        mReporter = getReporter();

        InOrder inOrder = inOrder(mAppStatusMonitor, mReportsHandler);
        inOrder.verify(mReportsHandler).reportPauseUserSession(mProcessConfiguration);
        inOrder.verify(mAppStatusMonitor).registerObserver(mObserverArgumentCaptor.capture(), eq(1000L));

        AppStatusMonitor.Observer observer = mObserverArgumentCaptor.getValue();
        observer.onResume();

        inOrder.verify(mReportsHandler).reportResumeUserSession(mProcessConfiguration);

        observer.onPause();

        inOrder.verify(mReportsHandler).reportPauseUserSession(mProcessConfiguration);

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void onEnableAutoTrackingAttemptOccurredWatching() {
        mMainReporter.onEnableAutoTrackingAttemptOccurred(ActivityLifecycleManager.WatchingStatus.WATCHING);
        verify(mPublicLogger).info("Enable activity auto tracking");
    }

    @Test
    public void testOnResumeSessionIsLogged() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED))
            .thenReturn(true);
        mMainReporter.resumeSession(activity);
        verify(mPublicLogger).info("Resume session");
    }

    @Test
    public void testOnResumeActivityShouldDispatchActivityNameToEventIfActivityIsNotNullStateChanged() {
        testOnResumeActivityShouldDispatchEventWithExpectedEventName(activity, activity.getClass().getSimpleName());
    }

    @Test
    public void testOnResumeActivityShouldDispatchNullToEventNameIfActivityIsNullStateChanged() {
        testOnResumeActivityShouldDispatchEventWithExpectedEventName(null, null);
    }

    private void testOnResumeActivityShouldDispatchEventWithExpectedEventName(
        Activity resumedActivity,
        String expectedEvent
    ) {
        when(activityStateManager.didStateChange(resumedActivity, ActivityStateManager.ActivityState.RESUMED))
            .thenReturn(true);
        when(EventsManager.notifyServiceOnActivityStartReportEntry(expectedEvent, mPublicLogger))
            .thenReturn(mockedEvent);
        mMainReporter.resumeSession(resumedActivity);
        verify(mReportsHandler).reportEvent(mockedEvent, mReporterEnvironment);
    }

    @Test
    public void testOnResumeActivityShouldDispatchActivityNameToEventIfActivityIsNotNullStateDidNotChange() {
        testOnResumeActivityShouldNotDispatchEvent(activity);
    }

    @Test
    public void testOnResumeActivityShouldDispatchNullToEventNameIfActivityIsNullStateDidNotChange() {
        testOnResumeActivityShouldNotDispatchEvent(null);
    }

    private void testOnResumeActivityShouldNotDispatchEvent(Activity resumedActivity) {
        when(activityStateManager.didStateChange(resumedActivity, ActivityStateManager.ActivityState.RESUMED))
            .thenReturn(false);
        mMainReporter.resumeSession(resumedActivity);
        verify(mReportsHandler, never()).reportEvent(any(CounterReport.class), any(ReporterEnvironment.class));
    }

    @Test
    public void testResumeSessionResumeAppStatusMonitorStateChanged() {
        when(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.RESUMED)).thenReturn(true);
        mMainReporter.resumeSession(null);
        verify(mAppStatusMonitor).resume();
    }

    @Test
    public void testResumeSessionResumeAppStatusMonitorStateDidNotChange() {
        when(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.RESUMED)).thenReturn(false);
        mMainReporter.resumeSession(null);
        verify(mAppStatusMonitor, never()).resume();
    }

    @Test
    public void testOnPauseSessionIsLogged() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.PAUSED)).thenReturn(true);
        mMainReporter.pauseSession(activity);
        verify(mPublicLogger).info("Pause session");
    }

    @Test
    public void testOnPauseActivityShouldDispatchActivityNameToEventIfActivityIsNotNullStateChanged() {
        testOnPauseActivityShouldDispatchEventWithExpectedEventName(activity, activity.getClass().getSimpleName());
    }

    @Test
    public void testOnPauseActivityShouldDispatchNullToEventNameIfActivityIsNullStateChanged() {
        testOnPauseActivityShouldDispatchEventWithExpectedEventName(null, null);
    }

    private void testOnPauseActivityShouldDispatchEventWithExpectedEventName(
        Activity pausedActivity,
        String expectedEvent
    ) {
        when(activityStateManager.didStateChange(pausedActivity, ActivityStateManager.ActivityState.PAUSED))
            .thenReturn(true);
        when(mReporterEnvironment.isForegroundSessionPaused()).thenReturn(false);
        when(EventsManager.activityEndReportEntry(expectedEvent, mPublicLogger)).thenReturn(mockedEvent);
        mMainReporter.pauseSession(pausedActivity);
        verify(mReportsHandler).reportEvent(mockedEvent, mReporterEnvironment);
    }

    @Test
    public void testOnPauseActivityShouldNotDispatchEventIfActivityIsNotNullStateDidNotChange() {
        testOnPauseActivityShouldNotDispatchEvent(activity);
    }

    @Test
    public void testOnPauseActivityShouldNotDispatchEventIfActivityIsNullStateDidNotChange() {
        testOnPauseActivityShouldNotDispatchEvent(null);
    }

    private void testOnPauseActivityShouldNotDispatchEvent(Activity pausedActivity) {
        when(activityStateManager.didStateChange(pausedActivity, ActivityStateManager.ActivityState.PAUSED))
            .thenReturn(false);
        when(mReporterEnvironment.isForegroundSessionPaused()).thenReturn(false);
        mMainReporter.pauseSession(pausedActivity);
        verify(mReportsHandler, never()).reportEvent(any(CounterReport.class), any(ReporterEnvironment.class));
    }

    @Test
    public void testPauseSessionPauseAppStatusMonitorStateChanged() {
        when(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.PAUSED)).thenReturn(true);
        mMainReporter.pauseSession(null);
        verify(mAppStatusMonitor).pause();
    }

    @Test
    public void testPauseSessionPauseAppStatusMonitorStateDidNotChange() {
        when(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.PAUSED)).thenReturn(false);
        mMainReporter.pauseSession(null);
        verify(mAppStatusMonitor, never()).pause();
    }

    @Override
    protected MainReporter getReporter() {
        when(mainReporterComponents.getContext()).thenReturn(mContext);
        when(mainReporterComponents.getReportsHandler()).thenReturn(mReportsHandler);
        when(mainReporterComponents.getReporterEnvironment()).thenReturn(mReporterEnvironment);
        when(mainReporterComponents.getExtraMetaInfoRetriever()).thenReturn(mExtraMetaInfoRetriever);
        when(mainReporterComponents.getProcessDetector()).thenReturn(processNameProvider);
        when(mainReporterComponents.getUnhandledExceptionConverter()).thenReturn(unhandledExceptionConverter);
        when(mainReporterComponents.getRegularErrorConverter()).thenReturn(regularErrorConverter);
        when(mainReporterComponents.getCustomErrorConverter()).thenReturn(customErrorConverter);
        when(mainReporterComponents.getAnrConverter()).thenReturn(anrConverter);
        when(mainReporterComponents.getPluginErrorDetailsConverter()).thenReturn(pluginErrorDetailsConverter);
        when(mainReporterComponents.getAppStatusMonitor()).thenReturn(mAppStatusMonitor);
        when(mainReporterComponents.getActivityStateManager()).thenReturn(activityStateManager);
        when(mainReporterComponents.getNativeCrashClient()).thenReturn(nativeCrashClient);
        mMainReporter = new MainReporter(mainReporterComponents);
        return mMainReporter;
    }

    @Test
    public void reportExternalAttribution() {
        byte[] bytes = new byte[]{1, 4, 7};
        ExternalAttribution attribution = mock(ExternalAttribution.class);
        when(attribution.toBytes()).thenReturn(bytes);
        when(EventsManager.clientExternalAttributionEntry(bytes, mPublicLogger)).thenReturn(mockedEvent);

        mMainReporter.reportExternalAttribution(attribution);

        verify(mReportsHandler).reportEvent(
            mockedEvent,
            mReporterEnvironment
        );
        verify(mPublicLogger).info("External attribution received: %s", attribution);
    }

    @Test
    public void testActivationAddErrorEnvironmentFromConfig() {
        MainReporter mainReporter = spy(mMainReporter);
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withErrorEnvironmentValue("memory", "2mb")
            .withErrorEnvironmentValue("money", "-100")
            .build();

        ArgumentCaptor<Map> argMap = ArgumentCaptor.forClass(Map.class);

        mainReporter.updateConfig(config, configExtension);

        verify(mainReporter, atLeast(1)).putAllToErrorEnvironment(argMap.capture());
        Map map = argMap.getValue();
        assertThat(map.size()).isEqualTo(2);
        assertThat(map.containsKey("memory")).isTrue();
        assertThat(map.containsKey("money")).isTrue();
        assertThat(map.containsValue("2mb")).isTrue();
        assertThat(map.containsValue("-100")).isTrue();
    }

    @Test
    public void testActivationAddAppEnvironmentFromConfig() throws Exception {
        MainReporter mainReporter = spy(mMainReporter);
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withAppEnvironmentValue("memory", "2mb")
            .withAppEnvironmentValue("money", "-100")
            .build();

        ArgumentCaptor<Map> argMap = ArgumentCaptor.forClass(Map.class);

        mainReporter.updateConfig(config, configExtension);

        verify(mainReporter, atLeast(1)).putAllToAppEnvironment(argMap.capture());
        Map map = argMap.getValue();
        assertThat(map.size()).isEqualTo(2);
        assertThat(map.containsKey("memory")).isTrue();
        assertThat(map.containsKey("money")).isTrue();
        assertThat(map.containsValue("2mb")).isTrue();
        assertThat(map.containsValue("-100")).isTrue();
    }

    @Test
    public void updateConfigWithEmptySubscribers() {
        when(configExtension.getAutoCollectedDataSubscribers()).thenReturn(Collections.emptyList());
        mMainReporter.updateConfig(AppMetricaConfig.newConfigBuilder(apiKey).build(), configExtension);
        verify(mCounterConfiguration, never()).addAutoCollectedDataSubscribers(any());
    }

    @Test
    public void updateConfigWithSubscribers() {
        List<String> subscribers = Arrays.asList("subscriber1", "subscriber2");
        when(configExtension.getAutoCollectedDataSubscribers()).thenReturn(subscribers);
        mMainReporter.updateConfig(AppMetricaConfig.newConfigBuilder(apiKey).build(), configExtension);
        verify(mCounterConfiguration).addAutoCollectedDataSubscribers(subscribers);
    }

    @Test
    public void testReportAppOpen() {
        String link = "some://uri";
        when(EventsManager.openAppReportEntry(link, true, mPublicLogger)).thenReturn(mockedEvent);
        mMainReporter.reportAppOpen(link, true);
        verify(mReportsHandler).reportEvent(mockedEvent, mReporterEnvironment);
        verify(mPublicLogger).info("App opened via deeplink: " + link);
    }

    @Test
    public void testResumeSessionStateChanged() {
        Activity activity = mock(Activity.class);
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED))
            .thenReturn(true);
        mMainReporter.resumeSession(activity);
    }

    @Test
    public void testResumeSessionStateDidNotChange() {
        Activity activity = mock(Activity.class);
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED))
            .thenReturn(false);
        mMainReporter.resumeSession(activity);
    }

    @Test
    public void testResumeSessionNullActivity() {
        when(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.RESUMED))
            .thenReturn(true);
        mMainReporter.resumeSession(null);
    }

    @Test
    public void testPauseSessionStateChanged() {
        Activity activity = mock(Activity.class);
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.PAUSED)).thenReturn(true);
        mMainReporter.pauseSession(activity);
    }

    @Test
    public void testPauseSessionStateDidNotChange() {
        Activity activity = mock(Activity.class);
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.PAUSED))
            .thenReturn(false);
        mMainReporter.pauseSession(activity);
    }

    @Test
    public void testPauseSessionNullActivity() {
        when(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.PAUSED))
            .thenReturn(true);
        mMainReporter.pauseSession(null);
    }

    @Test
    public void onWebViewReportingInit() {
        WebViewJsInterfaceHandler webViewJsInterfaceHandler = mock(WebViewJsInterfaceHandler.class);
        mMainReporter.onWebViewReportingInit(webViewJsInterfaceHandler);
        verify(webViewJsInterfaceHandler).setLogger(mPublicLogger);
    }

    @Test
    public void testReportUnhandledExceptionCrash() {
        final UnhandledException exception = mock(UnhandledException.class);
        mReporter.reportUnhandledException(exception);
        verify(mReportsHandler).reportCrash(exception, mReporterEnvironment);
    }

    @Test
    public void setNonNullLocation() {
        Location location = mock(Location.class);
        mMainReporter.setLocation(location);
        verify(mCounterConfiguration).setManualLocation(location);
        verify(mPublicLogger).info("Set location: %s", location);
    }

    @Test
    public void setNullLocation() {
        Location location = null;
        mMainReporter.setLocation(location);
        verify(mCounterConfiguration).setManualLocation(location);
        verify(mPublicLogger).info("Set location: %s", location);
    }

    @Test
    public void setAdvIdentifiersTracking() {
        mMainReporter.setAdvIdentifiersTracking(true, true);
        verify(mCounterConfiguration).setAdvIdentifiersTracking(true, true);
        verify(mPublicLogger).info("Set advIdentifiersTracking to %s", true);
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class ReporterReportCustomEventEventTypeTests extends
        BaseReporterTest.ReporterReportCustomEventEventTypeTests {

        @Mock
        private MainReporterComponents mainReporterComponents;

        public ReporterReportCustomEventEventTypeTests(int eventType, int wantedNumberOfInvocations) {
            super(eventType, wantedNumberOfInvocations);
        }

        @Override
        public BaseReporter getReporter() {
            when(mainReporterComponents.getContext()).thenReturn(mContext);
            when(mainReporterComponents.getReportsHandler()).thenReturn(mReportsHandler);
            when(mainReporterComponents.getReporterEnvironment()).thenReturn(mReporterEnvironment);
            when(mainReporterComponents.getProcessDetector()).thenReturn(processNameProvider);
            when(mainReporterComponents.getAppStatusMonitor()).thenReturn(mAppStatusMonitor);
            when(mainReporterComponents.getNativeCrashClient()).thenReturn(nativeCrashClient);
            return new MainReporter(
                mainReporterComponents
            );
        }
    }
}
