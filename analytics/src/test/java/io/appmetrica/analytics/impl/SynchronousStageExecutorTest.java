package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.webkit.WebView;
import io.appmetrica.analytics.AnrListener;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ExternalAttribution;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.crash.AppMetricaThrowable;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.UnhandledException;
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.plugins.PluginErrorDetails;
import io.appmetrica.analytics.profile.UserProfile;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SynchronousStageExecutorTest extends CommonTest {

    @Mock
    private PublicLogger publicLogger;
    @Mock
    private AppMetricaFacadeProvider mProvider;
    @Mock
    private AppMetricaFacade mAppMetricaFacade;
    @Mock
    private ECommerceEvent eCommerceEvent;
    @Mock
    private WebViewJsInterfaceHandler webViewJsInterfaceHandler;
    @Mock
    private SessionsTrackingManager sessionsTrackingManager;
    @Mock
    private ActivityLifecycleManager activityLifecycleManager;
    @Mock
    private PluginErrorDetails errorDetails;

    private SynchronousStageExecutor mSynchronousStageExecutor;
    private Context mContext;
    private final String mEventName = "EVENT_NAME";
    private final String mEventValue = "EVENT_VALUE";
    private final String apiKey = UUID.randomUUID().toString();

    @Rule
    public final ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();
    @Rule
    public final MockedStaticRule<LoggerStorage> loggerStorageMockedStaticRule =
        new MockedStaticRule<>(LoggerStorage.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(mProvider.getInitializedImpl(mContext)).thenReturn(mAppMetricaFacade);
        when(mProvider.peekInitializedImpl()).thenReturn(mAppMetricaFacade);
        when(LoggerStorage.getOrCreatePublicLogger(apiKey)).thenReturn(publicLogger);
        mSynchronousStageExecutor = new SynchronousStageExecutor(
                mProvider,
                webViewJsInterfaceHandler,
                activityLifecycleManager,
                sessionsTrackingManager,
                mock(ContextAppearedListener.class)
        );
    }

    @Test
    public void testPutAppEnvironmentValue() {
        mSynchronousStageExecutor.putAppEnvironmentValue("key", "value");
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testClearAppEnvironment() {
        mSynchronousStageExecutor.clearAppEnvironment();
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testSendEventsBuffer() {
        mSynchronousStageExecutor.sendEventsBuffer();
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testReportEvent() {
        mSynchronousStageExecutor.reportEvent(mEventName);
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testReportEventWithStringValue() {
        mSynchronousStageExecutor.reportEvent(mEventName, mEventValue);
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testReportEventAttributes() {
        mSynchronousStageExecutor.reportEvent(mEventName, new HashMap<String, Object>());
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testReportErrorNotNull() {
        final String message = "original message";
        Throwable throwable = new Throwable(message);
        throwable.fillInStackTrace();
        Throwable resultThrowable = mSynchronousStageExecutor.reportError(mEventName, throwable);
        assertThat(resultThrowable).isEqualToComparingFieldByField(throwable);
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testReportErrorNull() {
        Throwable resultThrowable = mSynchronousStageExecutor.reportError(mEventName, null);
        assertThat(resultThrowable).isExactlyInstanceOf(AppMetricaThrowable.class);
        StackTraceElement[] stacktrace = resultThrowable.getStackTrace();
        assertThat(stacktrace[0].getClassName()).isEqualTo("io.appmetrica.analytics.impl.SynchronousStageExecutor");
        assertThat(stacktrace[0].getMethodName()).isEqualTo("reportError");
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testReportUnhandledExceptionThrowable() {
        mSynchronousStageExecutor.reportUnhandledException(mock(Throwable.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testResumeSessionActivity() {
        mSynchronousStageExecutor.resumeSession(mock(Activity.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testPauseSessionActivity() {
        mSynchronousStageExecutor.pauseSession(mock(Activity.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testSetUserProfileID() {
        mSynchronousStageExecutor.setUserProfileID("id");
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testReportUserProfile() {
        mSynchronousStageExecutor.reportUserProfile(mock(UserProfile.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testReportRevenue() {
        mSynchronousStageExecutor.reportRevenue(mock(Revenue.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void reportECommerce() {
        mSynchronousStageExecutor.reportECommerce(eCommerceEvent);
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void setDataSendingEnabled() {
        mSynchronousStageExecutor.setDataSendingEnabled(true);
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testActivate() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
                .withAppEnvironmentValue("key", "value")
                .build();
        mSynchronousStageExecutor.activate(mContext, config);
        verify(mAppMetricaFacade).activateCore(config);
    }

    @Test
    public void activateIsLoggingIsSessionAutoTrackingEnabled() {
        when(publicLogger.isEnabled()).thenReturn(true);
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withSessionsAutoTrackingEnabled(true)
            .build();
        mSynchronousStageExecutor.activate(mContext, config);
        verify(publicLogger).i("Session auto tracking enabled");
    }

    @Test
    public void activateIsLoggingIsSessionAutoTrackingEnabledIfLoggerDisabled() {
        when(publicLogger.isEnabled()).thenReturn(false);
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withSessionsAutoTrackingEnabled(true)
            .build();
        mSynchronousStageExecutor.activate(mContext, config);
        verify(publicLogger, never()).i(anyString());
    }

    @Test
    public void activateIsLoggingIsSessionAutoTrackingDisabled() {
        when(publicLogger.isEnabled()).thenReturn(true);
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withSessionsAutoTrackingEnabled(false)
            .build();
        mSynchronousStageExecutor.activate(mContext, config);
        verify(publicLogger).i("Session auto tracking disabled");
    }

    @Test
    public void activateSetsUpActivityWatchingAutoTrackingEnabled() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
                .withSessionsAutoTrackingEnabled(true)
                .build();
        mSynchronousStageExecutor.activate(mContext, config);
        verify(sessionsTrackingManager).startWatching(true);
    }

    @Test
    public void activateSetsUpActivityWatchingAutoTrackingDisabled() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
                .withSessionsAutoTrackingEnabled(false)
                .build();
        mSynchronousStageExecutor.activate(mContext, config);
        verify(sessionsTrackingManager, never()).startWatching(anyBoolean());
    }

    @Test
    public void activateSetsUpActivityWatchingAutoTrackingNotSet() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey).build();
        mSynchronousStageExecutor.activate(mContext, config);
        verify(sessionsTrackingManager).startWatching(true);
    }

    @Test
    public void testEnableActivityAutoTracking() {
        Application application = mock(Application.class);
        when(sessionsTrackingManager.startWatching(false)).thenReturn(ActivityLifecycleManager.WatchingStatus.WATCHING);
        assertThat(mSynchronousStageExecutor.enableActivityAutoTracking(application))
                .isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
    }

    @Test
    public void testReportAppOpenActivity() {
        Activity activity = mock(Activity.class);
        Intent intent = mock(Intent.class);
        when(activity.getIntent()).thenReturn(intent);
        assertThat(mSynchronousStageExecutor.reportAppOpen(activity)).isSameAs(intent);
    }

    @Test
    public void testReportAppOpenNullActivity() {
        assertThat(mSynchronousStageExecutor.reportAppOpen((Activity) null)).isNull();
    }

    @Test
    public void testReportAppOpenActivityNullIntent() {
        Activity activity = mock(Activity.class);
        assertThat(mSynchronousStageExecutor.reportAppOpen(activity)).isNull();
    }

    @Test
    public void testReportAppOpenActivityGetIntentThrows() {
        Activity activity = mock(Activity.class);
        when(activity.getIntent()).thenThrow(new RuntimeException());
        assertThat(mSynchronousStageExecutor.reportAppOpen(activity)).isNull();
    }

    @Test
    public void testReportAppOpenIntent() {
        mSynchronousStageExecutor.reportAppOpen(mock(Intent.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testReportAppOpenDeeplink() {
        mSynchronousStageExecutor.reportAppOpen(mEventName);
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testSetLocation() {
        mSynchronousStageExecutor.setLocation(mock(Location.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testSetLocationTracking() {
        mSynchronousStageExecutor.setLocationTracking(true);
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testRequestDeferredDeeplinkParameters() {
        mSynchronousStageExecutor.requestDeferredDeeplinkParameters(mock(DeferredDeeplinkParametersListener.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testRequestDeferredDeeplink() {
        mSynchronousStageExecutor.requestDeferredDeeplink(mock(DeferredDeeplinkListener.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testInitialize() {
        mSynchronousStageExecutor.initialize(mContext);
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testInitializeWithConfig() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey).build();
        mSynchronousStageExecutor.initialize(mContext, config);
        verify(mAppMetricaFacade).activateCore(config);
    }

    @Test
    public void testPutErrorEnvironmentValue() {
        mSynchronousStageExecutor.putErrorEnvironmentValue("key", "value");
        verifyNoMoreInteractions(mProvider);

    }

    @Test
    public void testEnableAnrMonitoring() {
        mSynchronousStageExecutor.enableAnrMonitoring();
        verifyNoMoreInteractions(mProvider);

    }

    @Test
    public void testActivateReporter() {
        mSynchronousStageExecutor.activateReporter(mContext, mock(ReporterConfig.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testResumeSession() {
        mSynchronousStageExecutor.resumeSession();
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testPauseSession() {
        mSynchronousStageExecutor.pauseSession();
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testActivateWithReporterConfig() {
        mSynchronousStageExecutor.activate(mock(ReporterConfig.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testActivateWithApiKeyOnly() {
        mSynchronousStageExecutor.activate("apiKey");
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testReportUnhandledException() {
        mSynchronousStageExecutor.reportUnhandledException(mock(UnhandledException.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void testReportAnr() {
        mSynchronousStageExecutor.reportAnr(mock(AllThreads.class));
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void initWebViewReporting() {
        WebView webView = mock(WebView.class);
        AppMetricaProxy proxy = mock(AppMetricaProxy.class);
        mSynchronousStageExecutor.initWebViewReporting(webView, proxy);
        verify(webViewJsInterfaceHandler).initWebViewReporting(webView, proxy);
    }

    @Test
    public void reportJsEvent() {
        mSynchronousStageExecutor.reportJsEvent("name", "value");
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void reportJsInitEvent() {
        mSynchronousStageExecutor.reportJsInitEvent("value");
        verifyNoMoreInteractions(mProvider);
    }

    @Test
    public void reportPluginUnhandledException() {
        mSynchronousStageExecutor.reportPluginUnhandledException(errorDetails);
        verifyNoInteractions(mProvider, errorDetails);
    }

    @Test
    public void reportPluginError() {
        mSynchronousStageExecutor.reportPluginError(errorDetails, "message");
        verifyNoInteractions(mProvider, errorDetails);
    }

    @Test
    public void reportPluginErrorWithIdentifier() {
        mSynchronousStageExecutor.reportPluginError("id", "message", errorDetails);
        verifyNoInteractions(mProvider, errorDetails);
    }

    @Test
    public void getFeatures() {
        mSynchronousStageExecutor.getFeatures(mContext);
        verifyNoInteractions(mProvider);
    }

    @Test
    public void getUuid() {
        mSynchronousStageExecutor.getUuid(mContext);
        verifyNoInteractions(mProvider);
    }

    @Test
    public void registerAnrListener() {
        mSynchronousStageExecutor.registerAnrListener(mock(AnrListener.class));
        verifyNoInteractions(mProvider);
    }

    @Test
    public void reportExternalAttribution() {
        mSynchronousStageExecutor.reportExternalAttribution(mock(ExternalAttribution.class));
        verifyNoInteractions(mProvider);
    }
}
