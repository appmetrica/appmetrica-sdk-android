package io.appmetrica.analytics.impl.proxy.synchronous;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.webkit.WebView;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.impl.ActivityLifecycleManager;
import io.appmetrica.analytics.impl.AppMetricaFacade;
import io.appmetrica.analytics.impl.ContextAppearedListener;
import io.appmetrica.analytics.impl.SessionsTrackingManager;
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler;
import io.appmetrica.analytics.impl.crash.AppMetricaThrowable;
import io.appmetrica.analytics.impl.proxy.AppMetricaFacadeProvider;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy;
import io.appmetrica.analytics.impl.utils.LoggerStorage;
import io.appmetrica.analytics.impl.utils.PublicLogger;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.UUID;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SynchronousStageExecutorTest extends CommonTest {

    @Mock
    private Context mContext;
    @Mock
    private PublicLogger publicLogger;
    @Mock
    private AppMetricaFacadeProvider mProvider;
    @Mock
    private AppMetricaFacade mAppMetricaFacade;
    @Mock
    private WebViewJsInterfaceHandler webViewJsInterfaceHandler;
    @Mock
    private SessionsTrackingManager sessionsTrackingManager;
    @Mock
    private ActivityLifecycleManager activityLifecycleManager;
    @Mock
    private ContextAppearedListener contextAppearedListener;

    private SynchronousStageExecutor synchronousStageExecutor;
    private final String mEventName = "EVENT_NAME";
    private final String apiKey = UUID.randomUUID().toString();

    @Rule
    public final ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();
    @Rule
    public final MockedStaticRule<LoggerStorage> loggerStorageMockedStaticRule =
        new MockedStaticRule<>(LoggerStorage.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mProvider.getInitializedImpl(eq(mContext), anyBoolean())).thenReturn(mAppMetricaFacade);
        when(mProvider.peekInitializedImpl()).thenReturn(mAppMetricaFacade);
        when(LoggerStorage.getOrCreatePublicLogger(apiKey)).thenReturn(publicLogger);
        synchronousStageExecutor = new SynchronousStageExecutor(
            mProvider,
            webViewJsInterfaceHandler,
            activityLifecycleManager,
            sessionsTrackingManager,
            contextAppearedListener
        );
    }

    @Test
    public void reportError() {
        final String message = "original message";
        Throwable throwable = new Throwable(message);
        throwable.fillInStackTrace();

        Throwable resultThrowable = synchronousStageExecutor.reportError(mEventName, throwable);

        assertThat(resultThrowable).usingRecursiveComparison().isEqualTo(throwable);
    }

    @Test
    public void reportErrorIfThrowableIsNull() {
        Throwable resultThrowable = synchronousStageExecutor.reportError(mEventName, null);

        assertThat(resultThrowable).isExactlyInstanceOf(AppMetricaThrowable.class);
        StackTraceElement[] stacktrace = resultThrowable.getStackTrace();
        assertThat(stacktrace[0].getClassName())
            .isEqualTo("io.appmetrica.analytics.impl.proxy.synchronous.SynchronousStageExecutor");
        assertThat(stacktrace[0].getMethodName()).isEqualTo("reportError");
    }

    @Test
    public void activate() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withAppEnvironmentValue("key", "value")
            .build();

        synchronousStageExecutor.activate(mContext, config);

        verify(mAppMetricaFacade).activateCore(config);
    }

    @Test
    public void activateIsLoggingIsSessionAutoTrackingEnabled() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withSessionsAutoTrackingEnabled(true)
            .build();

        synchronousStageExecutor.activate(mContext, config);

        verify(publicLogger).info("Session auto tracking enabled");
        verify(contextAppearedListener).onProbablyAppeared(mContext);
    }

    @Test
    public void activateIsLoggingIsSessionAutoTrackingDisabled() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withSessionsAutoTrackingEnabled(false)
            .build();
        synchronousStageExecutor.activate(mContext, config);
        verify(publicLogger).info("Session auto tracking disabled");
    }

    @Test
    public void activateSetsUpActivityWatchingAutoTrackingEnabled() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withSessionsAutoTrackingEnabled(true)
            .build();

        synchronousStageExecutor.activate(mContext, config);

        verify(sessionsTrackingManager).startWatching(true);
    }

    @Test
    public void activateSetsUpActivityWatchingAutoTrackingDisabled() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey)
            .withSessionsAutoTrackingEnabled(false)
            .build();

        synchronousStageExecutor.activate(mContext, config);

        verify(sessionsTrackingManager, never()).startWatching(anyBoolean());
    }

    @Test
    public void activateSetsUpActivityWatchingAutoTrackingNotSet() {
        AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(apiKey).build();

        synchronousStageExecutor.activate(mContext, config);

        verify(sessionsTrackingManager).startWatching(true);
    }

    @Test
    public void enableActivityAutoTracking() {
        Application application = mock(Application.class);
        when(sessionsTrackingManager.startWatching(false)).thenReturn(ActivityLifecycleManager.WatchingStatus.WATCHING);

        final ActivityLifecycleManager.WatchingStatus status =
            synchronousStageExecutor.enableActivityAutoTracking(application);

        assertThat(status).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
    }

    @Test
    public void reportAppOpenActivity() {
        Activity activity = mock(Activity.class);
        Intent intent = mock(Intent.class);
        when(activity.getIntent()).thenReturn(intent);

        final Intent resultIntent = synchronousStageExecutor.reportAppOpen(activity);

        assertThat(resultIntent).isSameAs(intent);
    }

    @Test
    public void reportAppOpenIfActivityIsNull() {
        final Intent resultIntent = synchronousStageExecutor.reportAppOpen((Activity) null);

        assertThat(resultIntent).isNull();
    }

    @Test
    public void reportAppOpenIfActivityHasNullIntent() {
        Activity activity = mock(Activity.class);
        when(activity.getIntent()).thenReturn(null);

        final Intent resultIntent = synchronousStageExecutor.reportAppOpen(activity);

        assertThat(resultIntent).isNull();
    }

    @Test
    public void reportAppOpenIfActivityGetIntentThrows() {
        Activity activity = mock(Activity.class);
        when(activity.getIntent()).thenThrow(new RuntimeException());

        final Intent resultIntent = synchronousStageExecutor.reportAppOpen(activity);

        assertThat(resultIntent).isNull();
    }

    @Test
    public void getReporter() {
        final String apiKey = "some_key";
        synchronousStageExecutor.getReporter(mContext, apiKey);

        verify(contextAppearedListener).onProbablyAppeared(mContext);
    }

    @Test
    public void testActivateReporter() {
        synchronousStageExecutor.activateReporter(mContext, mock(ReporterConfig.class));

        verify(contextAppearedListener).onProbablyAppeared(mContext);
    }

    @Test
    public void initWebViewReporting() {
        WebView webView = mock(WebView.class);
        AppMetricaProxy proxy = mock(AppMetricaProxy.class);

        synchronousStageExecutor.initWebViewReporting(webView, proxy);

        verify(webViewJsInterfaceHandler).initWebViewReporting(webView, proxy);
    }

    @Test
    public void getUuid() {
        synchronousStageExecutor.getUuid(mContext);

        verify(contextAppearedListener).onProbablyAppeared(mContext);
    }
}
