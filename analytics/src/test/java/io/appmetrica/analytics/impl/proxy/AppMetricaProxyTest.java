package io.appmetrica.analytics.impl.proxy;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.ArrayMap;
import android.webkit.WebView;
import io.appmetrica.analytics.AdRevenue;
import io.appmetrica.analytics.AnrListener;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ExternalAttribution;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.ActivityLifecycleManager;
import io.appmetrica.analytics.impl.AppMetricaFacade;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.DeeplinkConsumer;
import io.appmetrica.analytics.impl.DefaultOneShotMetricaConfig;
import io.appmetrica.analytics.impl.MainReporter;
import io.appmetrica.analytics.impl.MainReporterApiConsumerProvider;
import io.appmetrica.analytics.impl.SessionsTrackingManager;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler;
import io.appmetrica.analytics.impl.proxy.synchronous.SynchronousStageExecutor;
import io.appmetrica.analytics.impl.proxy.validation.Barrier;
import io.appmetrica.analytics.impl.proxy.validation.SilentActivationValidator;
import io.appmetrica.analytics.impl.utils.validation.ValidationResult;
import io.appmetrica.analytics.impl.utils.validation.Validator;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessor;
import io.appmetrica.analytics.profile.UserProfile;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.StubbedBlockingExecutor;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaProxyTest extends CommonTest {

    @Mock
    private MainReporter mMainReporter;
    @Mock
    private MainReporterApiConsumerProvider mainReporterApiConsumerProvider;
    @Mock
    private DeeplinkConsumer deeplinkConsumer;
    @Mock
    private AppMetricaFacade mImpl;
    @Mock
    private AppMetricaFacadeProvider mProvider;
    @Mock
    private ReporterProxyStorage mReporterProxyStorage;
    @Mock
    private Barrier mBarrier;
    @Mock
    private Context context;
    @Mock
    private Context applicationContext;
    @Mock
    private Throwable mThrowable;
    @Mock
    private SynchronousStageExecutor mSynchronousStageExecutor;
    @Mock
    private DefaultOneShotMetricaConfig mDefaultOneShotMetricaConfig;
    @Mock
    private ECommerceEvent eCommerceEvent;
    @Mock
    private WebViewJsInterfaceHandler webViewJsInterfaceHandler;
    @Mock
    private SilentActivationValidator silentActivationValidator;
    @Mock
    private SessionsTrackingManager sessionsTrackingManager;

    @Rule
    public ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

    private IHandlerExecutor mStubbedExecutor;
    private AppMetricaProxy mProxy;

    private static final String ERROR_MESSAGE = "error message";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(context.getApplicationContext()).thenReturn(applicationContext);
        mStubbedExecutor = new StubbedBlockingExecutor();
        when(mProvider.peekInitializedImpl()).thenReturn(mImpl);
        when(mProvider.getInitializedImpl(applicationContext)).thenReturn(mImpl);
        when(ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor())
            .thenReturn(mStubbedExecutor);
        mProxy = createProxy();
        when(mImpl.getMainReporterApiConsumerProvider()).thenReturn(mainReporterApiConsumerProvider);
        doReturn(mMainReporter).when(mainReporterApiConsumerProvider).getMainReporter();
        doReturn(deeplinkConsumer).when(mainReporterApiConsumerProvider).getDeeplinkConsumer();
    }

    @Test
    public void testDefaultConstructor() {
        mProxy = new AppMetricaProxy();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(mProxy.getExecutor()).as("Executor")
            .isEqualTo(ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor());
        softAssertions.assertThat(mProxy.getProvider()).as("Provider").isNotNull();
        softAssertions.assertThat(mProxy.getMainFacadeBarrier()).as("Main facade barrier").isNotNull();
        softAssertions.assertThat(mProxy.getReporterProxyStorage()).as("Reporter proxy storage").isNotNull();
        softAssertions.assertAll();
    }

    @Test
    public void testActivate() {
        AppMetricaConfig config = mock(AppMetricaConfig.class);
        AppMetricaConfig mergedConfig = mock(AppMetricaConfig.class);
        when(mDefaultOneShotMetricaConfig.mergeWithUserConfig(config)).thenReturn(mergedConfig);
        mProxy.activate(context, config);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mProvider);
        order.verify(mBarrier).activate(context, config);
        order.verify(mSynchronousStageExecutor).activate(applicationContext, config);
        order.verify(mProvider).markActivated();
        verify(mImpl).activateFull(mergedConfig);
    }

    @Test
    public void testResumeSession() {
        Activity activity = mock(Activity.class);
        mProxy.resumeSession(activity);
        verify(mBarrier).resumeSession();
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, sessionsTrackingManager);
        order.verify(mBarrier).resumeSession();
        order.verify(mSynchronousStageExecutor).resumeSession(activity);
        order.verify(sessionsTrackingManager).resumeActivityManually(activity, mMainReporter);
    }

    @Test
    public void testPauseSession() {
        Activity activity = mock(Activity.class);
        mProxy.pauseSession(activity);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, sessionsTrackingManager);
        order.verify(mBarrier).pauseSession();
        order.verify(mSynchronousStageExecutor).pauseSession(activity);
        order.verify(sessionsTrackingManager).pauseActivityManually(activity, mMainReporter);
    }

    @Test
    public void testEnableActivityAutoTracking() {
        Application application = mock(Application.class);
        ActivityLifecycleManager.WatchingStatus status = ActivityLifecycleManager.WatchingStatus.WATCHING;
        when(sessionsTrackingManager.startWatchingIfNotYet()).thenReturn(status);
        mProxy.enableActivityAutoTracking(application);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).enableActivityAutoTracking(application);
        order.verify(mSynchronousStageExecutor).enableActivityAutoTracking(application);
        order.verify(mMainReporter).onEnableAutoTrackingAttemptOccurred(status);
    }

    @Test
    public void testReportEvent() {
        String name = "eventName";
        mProxy.reportEvent(name);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportEvent(name);
        order.verify(mSynchronousStageExecutor).reportEvent(name);
        order.verify(mMainReporter).reportEvent(name);
    }

    @Test
    public void testReportEventWithJson() {
        String name = "eventName";
        String json = "json";
        mProxy.reportEvent(name, json);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportEvent(name, json);
        order.verify(mSynchronousStageExecutor).reportEvent(name, json);
        order.verify(mMainReporter).reportEvent(name, json);
    }

    @Test
    public void testReportEventWithMap() {
        String name = "eventName";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", new Object());
        mProxy.reportEvent(name, map);
        ArgumentCaptor<LinkedHashMap<String, Object>> mapCaptor = ArgumentCaptor.forClass(LinkedHashMap.class);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportEvent(name, map);
        order.verify(mSynchronousStageExecutor).reportEvent(name, map);
        order.verify(mMainReporter).reportEvent(eq(name), mapCaptor.capture());
        assertThat(map).isEqualTo(mapCaptor.getValue());
    }

    @Test
    public void testReportEventWithArrayMap() {
        Map<String, Object> arrayMap = new ArrayMap<String, Object>();
        arrayMap.put("k1", "v1");
        arrayMap.put("k2", "v2");
        String name = "eventName";
        mProxy.reportEvent(name, arrayMap);
        ArgumentCaptor<LinkedHashMap<String, Object>> mapCaptor = ArgumentCaptor.forClass(LinkedHashMap.class);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportEvent(name, arrayMap);
        order.verify(mSynchronousStageExecutor).reportEvent(name, arrayMap);
        order.verify(mMainReporter).reportEvent(eq(name), mapCaptor.capture());
        assertThat(arrayMap).isEqualTo(mapCaptor.getValue());
    }

    @Test
    public void testReportEventWithNullMap() {
        String name = "eventName";
        mProxy.reportEvent(name, (Map) null);
        ArgumentCaptor<LinkedHashMap<String, Object>> mapCaptor = ArgumentCaptor.forClass(LinkedHashMap.class);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportEvent(name, (Map) null);
        order.verify(mSynchronousStageExecutor).reportEvent(name, (Map) null);
        order.verify(mMainReporter).reportEvent(eq(name), mapCaptor.capture());
        assertThat(mapCaptor.getValue()).isEmpty();
    }

    @Test
    public void testReportEventWithEmptyMap() {
        String name = "eventName";
        Map<String, Object> map = new HashMap<String, Object>();
        mProxy.reportEvent(name, map);
        ArgumentCaptor<LinkedHashMap<String, Object>> mapCaptor = ArgumentCaptor.forClass(LinkedHashMap.class);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportEvent(name, map);
        order.verify(mSynchronousStageExecutor).reportEvent(name, map);
        order.verify(mMainReporter).reportEvent(eq(name), mapCaptor.capture());
        assertThat(mapCaptor.getValue()).isEmpty();
    }

    @Test
    public void testReportEventWithAttributesMapChanged() {
        IHandlerExecutor executor = mock(IHandlerExecutor.class);
        when(ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor())
            .thenReturn(executor);
        mProxy = createProxy();
        final String name = "name";
        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("k1", "v1");
        mProxy.reportEvent(name, attributes);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        attributes.put("k1", "v2");
        runnableCaptor.getValue().run();
        verify(mMainReporter).reportEvent(eq(name), argThat(new ArgumentMatcher<Map<String, Object>>() {
            @Override
            public boolean matches(Map<String, Object> argument) {
                return argument.get("k1").equals("v1") && argument.size() == 1;
            }
        }));
    }

    @Test
    public void testReportUnhandledException() {
        Throwable exception = mock(Throwable.class);
        mProxy.reportUnhandledException(exception);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportUnhandledException(exception);
        order.verify(mSynchronousStageExecutor).reportUnhandledException(exception);
        order.verify(mMainReporter).reportUnhandledException(exception);
    }

    @Test
    public void testReportAppOpen() {
        Activity activity = mock(Activity.class);
        Intent intent = mock(Intent.class);
        when(mSynchronousStageExecutor.reportAppOpen(activity)).thenReturn(intent);
        mProxy.reportAppOpen(activity);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, deeplinkConsumer);
        order.verify(mBarrier).reportAppOpen(activity);
        order.verify(mSynchronousStageExecutor).reportAppOpen(activity);
        order.verify(deeplinkConsumer).reportAppOpen(intent);
    }

    @Test
    public void testReportAppOpenNullIntent() {
        doReturn(true).when(mProvider).isActivated();
        Activity activity = mock(Activity.class);
        when(mSynchronousStageExecutor.reportAppOpen(activity)).thenReturn(null);
        mProxy.reportAppOpen(activity);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, deeplinkConsumer);
        order.verify(mBarrier).reportAppOpen(activity);
        order.verify(mSynchronousStageExecutor).reportAppOpen(activity);
        order.verify(deeplinkConsumer).reportAppOpen((Intent) null);
    }

    @Test
    public void testReportAppOpenIntent() {
        Intent intent = mock(Intent.class);
        mProxy.reportAppOpen(intent);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, deeplinkConsumer);
        order.verify(mBarrier).reportAppOpen(intent);
        order.verify(mSynchronousStageExecutor).reportAppOpen(intent);
        order.verify(deeplinkConsumer).reportAppOpen(intent);
    }

    @Test
    public void testReportAppOpenString() {
        String appOpen = "appOpen";
        mProxy.reportAppOpen(appOpen);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, deeplinkConsumer);
        order.verify(mBarrier).reportAppOpen(appOpen);
        order.verify(mSynchronousStageExecutor).reportAppOpen(appOpen);
        order.verify(deeplinkConsumer).reportAppOpen(appOpen);
    }

    @Test
    public void testSetLocation() {
        Location location = mock(Location.class);
        mProxy.setLocation(location);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mProvider);
        order.verify(mBarrier).setLocation(location);
        order.verify(mSynchronousStageExecutor).setLocation(location);
        order.verify(mProvider).setLocation(location);
    }

    @Test
    public void testSetLocationTrackingForNonInitialized() {
        boolean locationTracking = true;
        mProxy.setLocationTracking(locationTracking);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mProvider);
        order.verify(mBarrier).setLocationTracking(locationTracking);
        order.verify(mSynchronousStageExecutor).setLocationTracking(locationTracking);
        order.verify(mProvider).setLocationTracking(locationTracking);
    }

    @Test
    public void setAdvIdentifiersTracking() {
        mProxy.setAdvIdentifiersTracking(true);
        InOrder inOrder = inOrder(mBarrier, mSynchronousStageExecutor, mProvider);
        inOrder.verify(mBarrier).setAdvIdentifiersTracking(true);
        inOrder.verify(mSynchronousStageExecutor).setAdvIdentifiersTracking(true);
        inOrder.verify(mProvider).setAdvIdentifiersTracking(true);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testRequestDeferredDeeplinkParameters() {
        DeferredDeeplinkParametersListener listener = mock(DeferredDeeplinkParametersListener.class);
        mProxy.requestDeferredDeeplinkParameters(listener);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mImpl);
        order.verify(mBarrier).requestDeferredDeeplinkParameters(listener);
        order.verify(mSynchronousStageExecutor).requestDeferredDeeplinkParameters(listener);
        order.verify(mImpl).requestDeferredDeeplinkParameters(listener);
    }

    @Test
    public void testRequestDeferredDeeplink() {
        DeferredDeeplinkListener listener = mock(DeferredDeeplinkListener.class);
        mProxy.requestDeferredDeeplink(listener);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mImpl);
        order.verify(mBarrier).requestDeferredDeeplink(listener);
        order.verify(mSynchronousStageExecutor).requestDeferredDeeplink(listener);
        order.verify(mImpl).requestDeferredDeeplink(listener);
    }

    @Test
    public void testSetUserProfileID() {
        doReturn(true).when(mProvider).isActivated();
        String userProfileID = "userProfileID";
        mProxy.setUserProfileID(userProfileID);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mProvider, mMainReporter);
        order.verify(mBarrier).setUserProfileID(userProfileID);
        order.verify(mSynchronousStageExecutor).setUserProfileID(userProfileID);
        order.verify(mProvider).setUserProfileID(userProfileID);
        order.verifyNoMoreInteractions();
    }

    public void testSetUserProfileIDNonActivated() {
        doReturn(false).when(mProvider).isActivated();
        String userProfileID = "userProfileID";
        mProxy.setUserProfileID(userProfileID);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mProvider, mMainReporter);
        order.verify(mBarrier).setUserProfileID(userProfileID);
        order.verify(mSynchronousStageExecutor).setUserProfileID(userProfileID);
        order.verify(mProvider).setUserProfileID(userProfileID);
        order.verifyNoMoreInteractions();
    }

    @Test
    public void testReportUserProfile() {
        UserProfile userProfile = mock(UserProfile.class);
        mProxy.reportUserProfile(userProfile);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportUserProfile(userProfile);
        order.verify(mSynchronousStageExecutor).reportUserProfile(userProfile);
        order.verify(mMainReporter).reportUserProfile(userProfile);
    }

    @Test
    public void testReportRevenue() {
        Revenue revenue = mock(Revenue.class);
        mProxy.reportRevenue(revenue);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportRevenue(revenue);
        order.verify(mSynchronousStageExecutor).reportRevenue(revenue);
        order.verify(mMainReporter).reportRevenue(revenue);
    }

    @Test
    public void testReportAdRevenue() {
        AdRevenue revenue = mock(AdRevenue.class);
        mProxy.reportAdRevenue(revenue);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportAdRevenue(revenue);
        order.verify(mSynchronousStageExecutor).reportAdRevenue(revenue);
        order.verify(mMainReporter).reportAdRevenue(revenue);
    }

    @Test
    public void reportECommerce() {
        mProxy.reportECommerce(eCommerceEvent);
        InOrder inOrder = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        inOrder.verify(mBarrier).reportECommerce(eCommerceEvent);
        inOrder.verify(mSynchronousStageExecutor).reportECommerce(eCommerceEvent);
        inOrder.verify(mMainReporter).reportECommerce(eCommerceEvent);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testGetReporter() {
        String apiKey = TestsData.generateApiKey();
        mProxy.getReporter(context, apiKey);
        InOrder inOrder = inOrder(mBarrier, mSynchronousStageExecutor, mReporterProxyStorage);
        inOrder.verify(mBarrier).getReporter(context, apiKey);
        inOrder.verify(mSynchronousStageExecutor).getReporter(applicationContext, apiKey);
        inOrder.verify(mReporterProxyStorage).getOrCreate(applicationContext, apiKey);
    }

    @Test
    public void activateReporter() {
        String apiKey = TestsData.generateApiKey();
        ReporterConfig config = ReporterConfig.newConfigBuilder(apiKey).withLogs().withSessionTimeout(15).build();
        ArgumentCaptor<ReporterConfig> syncConfigCaptor = ArgumentCaptor.forClass(ReporterConfig.class);
        ArgumentCaptor<ReporterConfig> providerConfigCaptor = ArgumentCaptor.forClass(ReporterConfig.class);
        mProxy.activateReporter(RuntimeEnvironment.getApplication(), config);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mReporterProxyStorage);
        order.verify(mBarrier).activateReporter(same(RuntimeEnvironment.getApplication()), syncConfigCaptor.capture());
        order.verify(mSynchronousStageExecutor).activateReporter(same(RuntimeEnvironment.getApplication()), syncConfigCaptor.capture());
        order.verify(mReporterProxyStorage).getOrCreate(same(RuntimeEnvironment.getApplication()), providerConfigCaptor.capture());
        assertThat(syncConfigCaptor.getValue()).isEqualToComparingFieldByField(config);
        assertThat(providerConfigCaptor.getValue()).isEqualToComparingFieldByField(config);

    }

    @Test
    public void testSendEventBuffer() {
        mProxy.sendEventsBuffer();
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).sendEventsBuffer();
        order.verify(mSynchronousStageExecutor).sendEventsBuffer();
        order.verify(mMainReporter).sendEventsBuffer();
    }

    @Test
    public void testReportError() {
        Throwable newThrowable = mock(Throwable.class);
        when(mSynchronousStageExecutor.reportError(ERROR_MESSAGE, mThrowable)).thenReturn(newThrowable);
        mProxy.reportError(ERROR_MESSAGE, mThrowable);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportError(ERROR_MESSAGE, mThrowable);
        order.verify(mSynchronousStageExecutor).reportError(ERROR_MESSAGE, mThrowable);
        order.verify(mMainReporter).reportError(ERROR_MESSAGE, newThrowable);
    }

    @Test
    public void testReportCustomError() {
        String id = "ididid";
        mProxy.reportError(id, ERROR_MESSAGE, mThrowable);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportError(id, ERROR_MESSAGE, mThrowable);
        order.verify(mSynchronousStageExecutor).reportError(id, ERROR_MESSAGE, mThrowable);
        order.verify(mMainReporter).reportError(id, ERROR_MESSAGE, mThrowable);
    }

    @Test
    public void dataSendingEnabledIfTrue() {
        checkSetDataSendingEnabled(true);
    }

    @Test
    public void dataSendingEnabledIfFalse() {
        checkSetDataSendingEnabled(false);
    }

    @Test
    public void testPutErrorEnvironmentValue() {
        String key = "key";
        String value = "value";
        mProxy.putErrorEnvironmentValue(key, value);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mProvider);
        order.verify(mBarrier).putErrorEnvironmentValue(key, value);
        order.verify(mSynchronousStageExecutor).putErrorEnvironmentValue(key, value);
        order.verify(mProvider).putErrorEnvironmentValue(key, value);
    }

    @Test
    public void initWebViewReporting() {
        WebView webView = mock(WebView.class);
        mProxy.initWebViewReporting(webView);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).initWebViewReporting(webView);
        order.verify(mSynchronousStageExecutor).initWebViewReporting(webView, mProxy);
        order.verify(mMainReporter).onWebViewReportingInit(webViewJsInterfaceHandler);
    }

    @Test
    public void reportJsEvent() {
        final String name = "My name";
        final String value = "My value";
        when(mBarrier.reportJsEvent(name, value)).thenReturn(true);
        mProxy.reportJsEvent(name, value);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportJsEvent(name, value);
        order.verify(mSynchronousStageExecutor).reportJsEvent(name, value);
        order.verify(mMainReporter).reportJsEvent(name, value);
    }

    @Test
    public void reportJsEventInvalidParameters() {
        final String name = "My name";
        final String value = "My value";
        when(mBarrier.reportJsEvent(name, value)).thenReturn(false);
        mProxy.reportJsEvent(name, value);
        InOrder order = inOrder(mBarrier);
        order.verify(mBarrier).reportJsEvent(name, value);
        verify(mSynchronousStageExecutor, never()).reportJsEvent(nullable(String.class), nullable(String.class));
        verify(mMainReporter, never()).reportJsEvent(nullable(String.class), nullable(String.class));
    }

    @Test
    public void reportJsInitEventSuccessful() {
        final String value = "My value";
        when(silentActivationValidator.validate()).thenReturn(ValidationResult.successful(mock(Validator.class)));
        when(mBarrier.reportJsInitEvent(value)).thenReturn(true);

        mProxy.reportJsInitEvent(value);
        InOrder order = inOrder(silentActivationValidator, mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(silentActivationValidator).validate();
        order.verify(mBarrier).reportJsInitEvent(value);
        order.verify(mSynchronousStageExecutor).reportJsInitEvent(value);
        order.verify(mMainReporter).reportJsInitEvent(value);
    }

    @Test
    public void reportJsInitEventBarrierFails() {
        final String value = "My value";
        when(silentActivationValidator.validate()).thenReturn(ValidationResult.successful(mock(Validator.class)));
        when(mBarrier.reportJsInitEvent(value)).thenReturn(false);

        mProxy.reportJsInitEvent(value);
        InOrder order = inOrder(silentActivationValidator, mBarrier);
        order.verify(silentActivationValidator).validate();
        order.verify(mBarrier).reportJsInitEvent(value);
        verifyNoMoreInteractions(mSynchronousStageExecutor, mMainReporter);
    }

    @Test
    public void reportJsInitEventNotActivated() {
        final String value = "My value";
        when(silentActivationValidator.validate()).thenReturn(ValidationResult.failed(mock(Validator.class), "error"));
        when(mBarrier.reportJsInitEvent(value)).thenReturn(true);

        mProxy.reportJsInitEvent(value);
        InOrder order = inOrder(silentActivationValidator);
        order.verify(silentActivationValidator).validate();
        verifyNoMoreInteractions(mBarrier, mSynchronousStageExecutor, mMainReporter);
    }

    @Test
    public void testGetDeviceId() {
        String deviceID = "deviceID";
        when(ClientServiceLocator.getInstance().getStartupParams(applicationContext).getDeviceId())
            .thenReturn(deviceID);

        assertThat(mProxy.getDeviceId(context)).isEqualTo(deviceID);
        verify(mBarrier).getDeviceId(context);
        verify(mSynchronousStageExecutor).getDeviceId(applicationContext);
    }

    @Test
    public void testGetDeviceIdNull() {
        when(ClientServiceLocator.getInstance().getStartupParams(applicationContext).getDeviceId())
            .thenReturn(null);

        assertThat(mProxy.getDeviceId(context)).isNull();
        verify(mBarrier).getDeviceId(context);
        verify(mSynchronousStageExecutor).getDeviceId(applicationContext);
    }

    @Test
    public void testGetUuid() {
        IdentifiersResult uuidResult = mock(IdentifiersResult.class);
        when(ClientServiceLocator.getInstance().getMultiProcessSafeUuidProvider(applicationContext).readUuid())
            .thenReturn(uuidResult);
        assertThat(mProxy.getUuid(context)).isEqualTo(uuidResult);
    }

    @Test
    public void testPutAppEnvironmentValue() {
        String key = "key";
        String value = "value";
        mProxy.putAppEnvironmentValue(key, value);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mProvider);
        order.verify(mBarrier).putAppEnvironmentValue(key, value);
        order.verify(mSynchronousStageExecutor).putAppEnvironmentValue(key, value);
        order.verify(mProvider).putAppEnvironmentValue(key, value);
    }

    @Test
    public void testClearAppEnvironment() {
        mProxy.clearAppEnvironment();
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mProvider);
        order.verify(mBarrier).clearAppEnvironment();
        order.verify(mSynchronousStageExecutor).clearAppEnvironment();
        order.verify(mProvider).clearAppEnvironment();
    }

    @Test
    public void registerAnrListener() {
        AnrListener listener = mock(AnrListener.class);
        mProxy.registerAnrListener(listener);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).registerAnrListener(eq(listener));
        order.verify(mSynchronousStageExecutor).registerAnrListener(eq(listener));
        order.verify(mMainReporter).registerAnrListener(eq(listener));
    }

    @Test
    public void reportExternalAttribution() {
        ExternalAttribution attribution = mock(ExternalAttribution.class);
        mProxy.reportExternalAttribution(attribution);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        order.verify(mBarrier).reportExternalAttribution(eq(attribution));
        order.verify(mSynchronousStageExecutor).reportExternalAttribution(eq(attribution));
        order.verify(mMainReporter).reportExternalAttribution(eq(attribution));
    }

    @Test
    public void reportExternalAdRevenue() {
        ModuleAdRevenueProcessor moduleAdRevenueProcessor = mock(ModuleAdRevenueProcessor.class);
        when(ClientServiceLocator.getInstance().getModulesController().getModuleAdRevenueProcessor())
            .thenReturn(moduleAdRevenueProcessor);

        String value = "string";
        mProxy.reportExternalAdRevenue(value);

        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, moduleAdRevenueProcessor);
        order.verify(mBarrier).reportExternalAdRevenue(value);
        order.verify(mSynchronousStageExecutor).reportExternalAdRevenue(value);
        order.verify(moduleAdRevenueProcessor).process(value);
    }

    @Test
    public void reportAnr() {
        Thread thread = mock(Thread.class);
        StackTraceElement[] stackTraceElements = new StackTraceElement[]{mock(StackTraceElement.class)};
        Map<Thread, StackTraceElement[]> allThreads = new HashMap<>();
        allThreads.put(thread, stackTraceElements);
        //noinspection unchecked
        ArgumentCaptor<Map<Thread, StackTraceElement[]>> allThreadsCaptor = ArgumentCaptor.forClass(Map.class);

        mProxy.reportAnr(allThreads);

        InOrder inOrder = inOrder(mBarrier, mSynchronousStageExecutor, mMainReporter);
        inOrder.verify(mBarrier).reportAnr(allThreads);
        inOrder.verify(mSynchronousStageExecutor).reportAnr(allThreads);
        inOrder.verify(mMainReporter).reportAnr(allThreadsCaptor.capture());
        inOrder.verifyNoMoreInteractions();

        assertThat(allThreadsCaptor.getValue())
            .isNotSameAs(allThreads)
            .containsExactlyEntriesOf(allThreads);
    }

    @Test
    public void warmUpForSelfProcess() {
        mProxy.warmUpForSelfProcess(context);
        InOrder inOrder = inOrder(mBarrier, mSynchronousStageExecutor, mProvider, mMainReporter);
        inOrder.verify(mBarrier).warmUpForSelfProcess(context);
        inOrder.verify(mSynchronousStageExecutor).warmUpForSelfReporter(context);
        inOrder.verify(mProvider).getInitializedImpl(context);
        inOrder.verifyNoMoreInteractions();
    }

    private void checkSetDataSendingEnabled(boolean value) {
        mProxy.setDataSendingEnabled(value);
        InOrder order = inOrder(mBarrier, mSynchronousStageExecutor, mProvider);
        order.verify(mBarrier).setDataSendingEnabled(value);
        order.verify(mSynchronousStageExecutor).setDataSendingEnabled(value);
        order.verify(mProvider).setDataSendingEnabled(value);
    }

    private AppMetricaProxy createProxy() {
        return new AppMetricaProxy(
            mProvider,
            mBarrier,
            silentActivationValidator,
            webViewJsInterfaceHandler,
            mSynchronousStageExecutor,
            mReporterProxyStorage,
            mDefaultOneShotMetricaConfig,
            sessionsTrackingManager
        );
    }

}
