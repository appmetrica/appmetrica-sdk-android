package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.AnrListener;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ExternalAttribution;
import io.appmetrica.analytics.PreloadInfo;
import io.appmetrica.analytics.ValidationException;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.crash.ANRMonitor;
import io.appmetrica.analytics.impl.crash.PluginErrorDetailsConverter;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.crash.client.converter.AnrConverter;
import io.appmetrica.analytics.impl.crash.client.converter.CustomErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.RegularErrorConverter;
import io.appmetrica.analytics.impl.crash.client.converter.UnhandledExceptionConverter;
import io.appmetrica.analytics.impl.preloadinfo.PreloadInfoWrapper;
import io.appmetrica.analytics.impl.reporter.MainReporterContext;
import io.appmetrica.analytics.impl.reporter.ReporterLifecycleListener;
import io.appmetrica.analytics.impl.startup.StartupHelper;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class MainReporterTest extends BaseReporterTest {

    private StartupHelper mStartupHelper;
    @Mock
    private IReporterExtended mAppmetricaReporter;
    @Mock
    private IReporterExtended mPushReporter;
    @Mock
    private LibraryAnrDetector mLibraryAnrDetector;
    @Mock
    private UnhandledSituationReporterProvider mAppmetricaReporterProvider;
    @Mock
    private UnhandledSituationReporterProvider mPushReporterProvider;
    @Mock
    private ICommonExecutor mExecutor;
    @Captor
    private ArgumentCaptor<AppStatusMonitor.Observer> mObserverArgumentCaptor;
    @Mock
    private ReporterEnvironment mAnotherReporterEnvironment;
    @Mock
    private ClientServiceLocator clientServiceLocator;
    @Mock
    private ActivityStateManager activityStateManager;
    @Mock
    private Activity activity;
    @Mock
    private ReporterLifecycleListener reporterLifecycleListener;
    @Captor
    private ArgumentCaptor<PreloadInfoWrapper> mPreloadInfoWrapperCaptor;

    private MainReporter mMainReporter;

    private static final String DEVICE_ID = "1pl123op1k23pok13p1";

    @Before
    @Override
    public void setUp() {
        mStartupHelper = mock(StartupHelper.class);
        super.setUp();
        when(mAnotherReporterEnvironment.getProcessConfiguration()).thenReturn(mProcessConfiguration);
        when(mAnotherReporterEnvironment.getReporterConfiguration()).thenReturn(mCounterConfiguration);
        when(mStartupHelper.getDeviceId()).thenReturn(DEVICE_ID);
        when(mAppmetricaReporterProvider.getReporter()).thenReturn(mAppmetricaReporter);
        when(mPushReporterProvider.getReporter()).thenReturn(mPushReporter);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }
        }).when(mExecutor).execute(any(Runnable.class));
    }

    @Test
    public void testMainReporterListenerIsCalledInConstructor() {
        when(ClientServiceLocator.getInstance().getReporterLifecycleListener())
                .thenReturn(reporterLifecycleListener);
        final MainReporter reporter = getReporter();
        verify(reporterLifecycleListener).onCreateMainReporter(any(MainReporterContext.class));
    }

    @Test
    public void testDefaultSessionTimeoutIsLoggedInConstructor() {
        when(mPublicLogger.isEnabled()).thenReturn(true);
        final MainReporter reporter = getReporter();
        verify(mPublicLogger).i("Actual sessions timeout is 10000");
    }

    @Test
    public void testDefaultSessionTimeoutIsLoggedInConstructorIfLoggerIsDisabled() {
        when(mPublicLogger.isEnabled()).thenReturn(false);
        final MainReporter reporter = getReporter();
        verify(mPublicLogger, never()).i(anyString());
    }

    @Test
    public void testPreloadInfoWrapperSet() {
        PreloadInfo preloadInfo = mock(PreloadInfo.class);
        mMainReporter = getReporter(
                AppMetricaConfig.newConfigBuilder(apiKey)
                        .withPreloadInfo(preloadInfo)
                        .withAdditionalConfig("YMM_clids", new HashMap<String, String>())
                        .withAdditionalConfig("YMM_preloadInfoAutoTracking", true)
                        .build(),
                mAnotherReporterEnvironment
        );
        verify(mAnotherReporterEnvironment).setPreloadInfoWrapper(mPreloadInfoWrapperCaptor.capture());
        assertThat(mPreloadInfoWrapperCaptor.getValue()).isEqualToComparingFieldByField(new PreloadInfoWrapper(preloadInfo, mPublicLogger, true));
    }

    @Test
    public void testPreloadInfoWrapperSetAutoTrackingIsFalse() {
        PreloadInfo preloadInfo = mock(PreloadInfo.class);
        mMainReporter = getReporter(
                AppMetricaConfig.newConfigBuilder(apiKey)
                        .withPreloadInfo(preloadInfo)
                        .withAdditionalConfig("YMM_clids", new HashMap<String, String>())
                        .withAdditionalConfig("YMM_preloadInfoAutoTracking", false)
                        .build(),
                mAnotherReporterEnvironment
        );
        verify(mAnotherReporterEnvironment).setPreloadInfoWrapper(mPreloadInfoWrapperCaptor.capture());
        assertThat(mPreloadInfoWrapperCaptor.getValue()).isEqualToComparingFieldByField(new PreloadInfoWrapper(preloadInfo, mPublicLogger, false));
    }

    @Test
    public void testPreloadInfoWrapperSetAutoTrackingIsNull() {
        PreloadInfo preloadInfo = mock(PreloadInfo.class);
        mMainReporter = getReporter(
                AppMetricaConfig.newConfigBuilder(apiKey)
                        .withPreloadInfo(preloadInfo)
                        .build(),
                mAnotherReporterEnvironment
        );
        verify(mAnotherReporterEnvironment).setPreloadInfoWrapper(mPreloadInfoWrapperCaptor.capture());
        assertThat(mPreloadInfoWrapperCaptor.getValue()).isEqualToComparingFieldByField(new PreloadInfoWrapper(preloadInfo, mPublicLogger, false));
    }

    @Test
    public void testPreloadInfoWrapperSetPreloadInfoIsNull() {
        mMainReporter = getReporter(
                AppMetricaConfig.newConfigBuilder(apiKey)
                        .withAdditionalConfig("YMM_clids", new HashMap<String, String>())
                        .withAdditionalConfig("YMM_preloadInfoAutoTracking", true)
                        .build(),
                mAnotherReporterEnvironment
        );
        verify(mAnotherReporterEnvironment).setPreloadInfoWrapper(mPreloadInfoWrapperCaptor.capture());
        assertThat(mPreloadInfoWrapperCaptor.getValue()).isEqualToComparingFieldByField(new PreloadInfoWrapper(null, mPublicLogger, false));
    }

    @Test
    public void userProfileIDIfNotSet() {
        mMainReporter = createWithProfileID(null);
        assertThat(mMainReporter.getEnvironment().getInitialUserProfileID()).isNull();
    }

    @Test
    public void userProfileID() {
        String userProfileID = "user_profile_id";
        mMainReporter = createWithProfileID(userProfileID);
        assertThat(mMainReporter.getEnvironment().getInitialUserProfileID()).isEqualTo(userProfileID);
    }

    private MainReporter createWithProfileID(String userProfileID) {
        when(mPublicLogger.isEnabled()).thenReturn(false);
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
                .withUserProfileID(userProfileID)
                .build();
        return new MainReporter(
                mContext, mProcessConfiguration, config, mReportsHandler, nativeCrashClient, mStartupHelper,
                mAppmetricaReporterProvider, mPushReporterProvider, clientServiceLocator, mExtraMetaInfoRetriever
        );
    }

    @Test
    public void testNativeCrashReportingEnabled() {
        Mockito.reset(nativeCrashClient);
        mConfig = AppMetricaConfig.newConfigBuilder(apiKey).withNativeCrashReporting(true).build();
        String errorEnv = "erroorrenen";
        doReturn(errorEnv).when(mReporterEnvironment).getErrorEnvironment();
        mMainReporter = getReporter();
        verify(nativeCrashClient).initHandling(eq(mContext), eq(apiKey), eq(errorEnv));
    }

    @Test
    public void testNativeCrashReportingNotSet() {
        Mockito.reset(nativeCrashClient);
        mConfig = AppMetricaConfig.newConfigBuilder(apiKey).build();
        mMainReporter = getReporter();
        verify(nativeCrashClient).initHandling(eq(mContext), eq(apiKey), eq((String) null));
    }

    @Test
    public void testNativeCrashReportingDisabled() {
        mConfig = AppMetricaConfig.newConfigBuilder(apiKey).withNativeCrashReporting(false).build();
        mMainReporter = getReporter();
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
        inOrder.verify(mAppStatusMonitor).registerObserver(mObserverArgumentCaptor.capture(), eq(5000L));

        AppStatusMonitor.Observer observer = mObserverArgumentCaptor.getValue();
        observer.onResume();

        inOrder.verify(mReportsHandler).reportResumeUserSession(mProcessConfiguration);

        observer.onPause();

        inOrder.verify(mReportsHandler).reportPauseUserSession(mProcessConfiguration);

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void onEnableAutoTrackingAttemptOccurredWatching() {
        when(mPublicLogger.isEnabled()).thenReturn(true);
        mMainReporter.onEnableAutoTrackingAttemptOccurred(ActivityLifecycleManager.WatchingStatus.WATCHING);
        verify(mPublicLogger).i("Enable activity auto tracking");
    }

    @Test
    public void testOnResumeSessionIsLogged() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED)).thenReturn(true);
        when(mPublicLogger.isEnabled()).thenReturn(true);
        mMainReporter.resumeSession(activity);
        verify(mPublicLogger).i("Resume session");
    }

    @Test
    public void testOnResumeSessionIsNotLoggedIfLoggerIsDisabled() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED)).thenReturn(true);
        when(mPublicLogger.isEnabled()).thenReturn(false);
        mMainReporter.resumeSession(activity);
        verify(mPublicLogger, never()).i(anyString());
    }

    @Test
    public void testOnResumeActivityShouldDispatchActivityNameToEventIfActivityIsNotNullStateChanged() {
        testOnResumeActivityShouldDispatchEventWithExpectedEventName(activity, activity.getClass().getSimpleName());
    }

    @Test
    public void testOnResumeActivityShouldDispatchNullToEventNameIfActivityIsNullStateChanged() {
        testOnResumeActivityShouldDispatchEventWithExpectedEventName(null, null);
    }

    private void testOnResumeActivityShouldDispatchEventWithExpectedEventName(Activity resumedActivity, String expectedEvent) {
        when(activityStateManager.didStateChange(resumedActivity, ActivityStateManager.ActivityState.RESUMED)).thenReturn(true);
        when(EventsManager.notifyServiceOnActivityStartReportEntry(expectedEvent, mPublicLogger)).thenReturn(mockedEvent);
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
        when(activityStateManager.didStateChange(resumedActivity, ActivityStateManager.ActivityState.RESUMED)).thenReturn(false);
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
        when(mPublicLogger.isEnabled()).thenReturn(true);
        mMainReporter.pauseSession(activity);
        verify(mPublicLogger).i("Pause session");
    }

    @Test
    public void testOnPauseSessionIsNotLoggedIfLoggerIsDisabled() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.PAUSED)).thenReturn(true);
        when(mPublicLogger.isEnabled()).thenReturn(false);
        mMainReporter.pauseSession(activity);
        verify(mPublicLogger, never()).i(anyString());
    }

    @Test
    public void testOnPauseActivityShouldDispatchActivityNameToEventIfActivityIsNotNullStateChanged() {
        testOnPauseActivityShouldDispatchEventWithExpectedEventName(activity, activity.getClass().getSimpleName());
    }

    @Test
    public void testOnPauseActivityShouldDispatchNullToEventNameIfActivityIsNullStateChanged() {
        testOnPauseActivityShouldDispatchEventWithExpectedEventName(null, null);
    }

    private void testOnPauseActivityShouldDispatchEventWithExpectedEventName(Activity pausedActivity, String expectedEvent) {
        when(activityStateManager.didStateChange(pausedActivity, ActivityStateManager.ActivityState.PAUSED)).thenReturn(true);
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
        when(activityStateManager.didStateChange(pausedActivity, ActivityStateManager.ActivityState.PAUSED)).thenReturn(false);
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
        return getReporter(mConfig, mReporterEnvironment);
    }

    private MainReporter getReporter(@NonNull AppMetricaConfig config, @NonNull ReporterEnvironment environment) {
        mMainReporter = new MainReporter(
                mContext,
                config,
                mReportsHandler,
                nativeCrashClient,
                environment,
                mAppStatusMonitor,
                mStartupHelper,
                mLibraryAnrDetector,
                processDetector,
                mAppmetricaReporterProvider,
                mPushReporterProvider,
                mExecutor,
                mExtraMetaInfoRetriever,
                activityStateManager,
                pluginErrorDetailsConverter,
                unhandledExceptionConverter,
                regularErrorConverter,
                customErrorConverter,
                anrConverter
        );
        return mMainReporter;
    }

    @Test
    public void testAnrMonitoring() {
        ANRMonitor anrMonitor = mock(ANRMonitor.class);
        mMainReporter.setAnrMonitor(anrMonitor);
        mMainReporter.enableAnrMonitoring();
        verify(anrMonitor).startMonitoring();
    }

    @Test
    public void testAnrMonitoringCalledOnlyOnce() {
        ANRMonitor anrMonitor = mock(ANRMonitor.class);
        mMainReporter.setAnrMonitor(anrMonitor);
        mMainReporter.enableAnrMonitoring();
        mMainReporter.enableAnrMonitoring();
        verify(anrMonitor, times(1)).startMonitoring();
        verifyNoMoreInteractions(anrMonitor);
    }

    @Test
    public void testRegisterAnrListener() {
        AnrListener listener1 = mock(AnrListener.class);
        AnrListener listener2 = mock(AnrListener.class);
        ANRMonitor anrMonitor = mock(ANRMonitor.class);
        mMainReporter.setAnrMonitor(anrMonitor);
        mMainReporter.registerAnrListener(listener1);
        mMainReporter.registerAnrListener(listener2);

        ArgumentCaptor<ANRMonitor.Listener> anrListenerCaptor = ArgumentCaptor.forClass(ANRMonitor.Listener.class);
        verify(anrMonitor, times(2)).subscribe(anrListenerCaptor.capture());

        ANRMonitor.Listener anrListener1 = anrListenerCaptor.getAllValues().get(0);
        ANRMonitor.Listener anrListener2 = anrListenerCaptor.getAllValues().get(1);

        anrListener1.onAppNotResponding();
        verify(listener1).onAppNotResponding();
        verifyNoMoreInteractions(listener2);

        anrListener2.onAppNotResponding();
        verify(listener2).onAppNotResponding();
        verifyNoMoreInteractions(listener1);
    }

    @Test
    public void reportExternalAttribution() {
        when(mPublicLogger.isEnabled()).thenReturn(true);

        byte[] bytes = new byte[]{1, 4, 7};
        ExternalAttribution attribution = mock(ExternalAttribution.class);
        when(attribution.toBytes()).thenReturn(bytes);
        when(EventsManager.clientExternalAttributionEntry(bytes, mPublicLogger)).thenReturn(mockedEvent);

        mMainReporter.reportExternalAttribution(attribution);

        verify(mReportsHandler).reportEvent(
            mockedEvent,
            mReporterEnvironment
        );
        verify(mPublicLogger).fi("External attribution received: %s", attribution);
    }

    @Test
    public void testActivationAddErrorEnvironmentFromConfig() throws Exception {
        MainReporter mainReporter = spy(mMainReporter);
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
                .withErrorEnvironmentValue("memory", "2mb")
                .withErrorEnvironmentValue("money", "-100")
                .build();

        ArgumentCaptor<Map> argMap = ArgumentCaptor.forClass(Map.class);

        mainReporter.updateConfig(config, false);

        verify(mainReporter, atLeast(1)).putAllToErrorEnvironment(argMap.capture());
        Map map = argMap.getValue();
        assertThat(map.size()).isEqualTo(2);
        assertThat(map.keySet().contains("memory")).isTrue();
        assertThat(map.keySet().contains("money")).isTrue();
        assertThat(map.values().contains("2mb")).isTrue();
        assertThat(map.values().contains("-100")).isTrue();
    }

    @Test
    public void testActivationAddAppEnvironmentFromConfig() throws Exception {
        MainReporter mainReporter = spy(mMainReporter);
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
                .withAppEnvironmentValue("memory", "2mb")
                .withAppEnvironmentValue("money", "-100")
                .build();

        ArgumentCaptor<Map> argMap = ArgumentCaptor.forClass(Map.class);

        mainReporter.updateConfig(config, false);

        verify(mainReporter, atLeast(1)).putAllToAppEnvironment(argMap.capture());
        Map map = argMap.getValue();
        assertThat(map.size()).isEqualTo(2);
        assertThat(map.keySet().contains("memory")).isTrue();
        assertThat(map.keySet().contains("money")).isTrue();
        assertThat(map.values().contains("2mb")).isTrue();
        assertThat(map.values().contains("-100")).isTrue();
    }

    @Test
    public void testReportOnlyAppmetricaAnr() {
        when(mLibraryAnrDetector.isAppmetricaAnr(any(List.class))).thenReturn(true);
        when(mLibraryAnrDetector.isPushAnr(any(List.class))).thenReturn(false);
        mMainReporter.getAnrMonitor().handleAppNotResponding();
        verify(mAppmetricaReporter).reportAnr(any(AllThreads.class));
        verify(mPushReporter, never()).reportAnr(any(AllThreads.class));
    }

    @Test
    public void testReportOnlyPushAnr() {
        when(mLibraryAnrDetector.isAppmetricaAnr(any(List.class))).thenReturn(false);
        when(mLibraryAnrDetector.isPushAnr(any(List.class))).thenReturn(true);
        mMainReporter.getAnrMonitor().handleAppNotResponding();
        verify(mAppmetricaReporter, never()).reportAnr(any(AllThreads.class));
        verify(mPushReporter).reportAnr(any(AllThreads.class));
    }

    @Test
    public void testReportAppmetricaAndPushAnr() {
        when(mLibraryAnrDetector.isAppmetricaAnr(any(List.class))).thenReturn(true);
        when(mLibraryAnrDetector.isPushAnr(any(List.class))).thenReturn(true);
        mMainReporter.getAnrMonitor().handleAppNotResponding();
        verify(mAppmetricaReporter).reportAnr(any(AllThreads.class));
        verify(mPushReporter).reportAnr(any(AllThreads.class));
    }

    @Test
    public void testReportNeitherAppmetricaNorPushAnr() {
        when(mLibraryAnrDetector.isAppmetricaAnr(any(List.class))).thenReturn(false);
        when(mLibraryAnrDetector.isPushAnr(any(List.class))).thenReturn(false);
        mMainReporter.getAnrMonitor().handleAppNotResponding();
        verify(mAppmetricaReporter, never()).reportAnr(any(AllThreads.class));
        verify(mPushReporter, never()).reportAnr(any(AllThreads.class));
    }

    @Test
    public void testReportAppOpen() throws Exception {
        when(mPublicLogger.isEnabled()).thenReturn(true);
        String link = "some://uri";
        when(EventsManager.openAppReportEntry(link, true, mPublicLogger)).thenReturn(mockedEvent);
        mMainReporter.reportAppOpen(link, true);
        verify(mReportsHandler).reportEvent(mockedEvent, mReporterEnvironment);
        verify(mPublicLogger).i("App opened via deeplink: " + link);
    }

    //region MainReporter#reportReferralUrl(String)

    @Test
    public void testReportReferralUrl() throws Exception {
        String referralUrl = "some://uri";
        when(EventsManager.referralUrlReportEntry(eq(referralUrl), any(PublicLogger.class))).thenReturn(mockedEvent);
        mMainReporter.reportReferralUrl(referralUrl);
        verify(mReportsHandler).reportEvent(same(mockedEvent), any(ReporterEnvironment.class));
    }

    @Test(expected = ValidationException.class)
    public void testReportReferralUrlForNull() throws Exception {
        mMainReporter.reportReferralUrl(null);
        verifyNoMoreInteractions(mReportsHandler);
    }

    @Test(expected = ValidationException.class)
    public void testReportReferralUrlForEmptyString() throws Exception {
        mMainReporter.reportReferralUrl("");
        verifyNoMoreInteractions(mReportsHandler);
    }
    //endregion

    @Test
    public void testResumeSessionStateChanged() throws Throwable {
        Activity activity = mock(Activity.class);
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED)).thenReturn(true);
        mMainReporter.resumeSession(activity);
    }

    @Test
    public void testResumeSessionStateDidNotChange() throws Throwable {
        Activity activity = mock(Activity.class);
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED)).thenReturn(false);
        mMainReporter.resumeSession(activity);
    }

    @Test
    public void testResumeSessionNullActivity() throws Throwable {
        when(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.RESUMED)).thenReturn(true);
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
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.PAUSED)).thenReturn(false);
        mMainReporter.pauseSession(activity);
    }

    @Test
    public void testPauseSessionNullActivity() {
        when(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.PAUSED)).thenReturn(true);
        mMainReporter.pauseSession(null);
    }

    @Test
    public void testReporterType() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey).build();
        when(LoggerStorage.getOrCreatePublicLogger(TestsData.UUID_API_KEY)).thenReturn(mPublicLogger);
        mReporter = new MainReporter(
                mContext,
                mProcessConfiguration,
                config,
                mReportsHandler,
                nativeCrashClient,
                mStartupHelper,
                mAppmetricaReporterProvider,
                mPushReporterProvider,
                clientServiceLocatorRule.instance,
                mExtraMetaInfoRetriever
        );
        assertThat(mReporter.getEnvironment().getReporterConfiguration().getReporterType()).isEqualTo(CounterConfigurationReporterType.MAIN);
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
        when(mPublicLogger.isEnabled()).thenReturn(true);
        mMainReporter.setLocation(location);
        verify(mCounterConfiguration).setManualLocation(location);
        verify(mPublicLogger).fi("Set location: %s", location);
    }

    @Test
    public void setNullLocation() {
        Location location = null;
        when(mPublicLogger.isEnabled()).thenReturn(true);
        mMainReporter.setLocation(location);
        verify(mCounterConfiguration).setManualLocation(location);
        verify(mPublicLogger).fi("Set location: %s", location);
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class ReporterReportCustomEventEventTypeTests extends BaseReporterTest.ReporterReportCustomEventEventTypeTests {

        public ReporterReportCustomEventEventTypeTests(int eventType, int wantedNumberOfInvocations) {
            super(eventType, wantedNumberOfInvocations);
        }

        @Override
        public BaseReporter getReporter() {
            return new MainReporter(
                    mContext,
                    mConfig,
                    mReportsHandler,
                    nativeCrashClient,
                    mReporterEnvironment,
                    mAppStatusMonitor,
                    mStartupHelper,
                    mock(LibraryAnrDetector.class),
                    processDetector,
                    mock(UnhandledSituationReporterProvider.class),
                    mock(UnhandledSituationReporterProvider.class),
                    mock(ICommonExecutor.class),
                    mock(ExtraMetaInfoRetriever.class),
                    mock(ActivityStateManager.class),
                    mock(PluginErrorDetailsConverter.class),
                    mock(UnhandledExceptionConverter.class),
                    mock(RegularErrorConverter.class),
                    mock(CustomErrorConverter.class),
                    mock(AnrConverter.class)
            );
        }
    }
}
