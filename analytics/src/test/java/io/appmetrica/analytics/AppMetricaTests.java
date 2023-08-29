package io.appmetrica.analytics;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.webkit.WebView;
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.AppMetricaPluginsImplProvider;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxy;
import io.appmetrica.analytics.impl.proxy.AppMetricaProxyProvider;
import io.appmetrica.analytics.internal.IdentifiersResult;
import io.appmetrica.analytics.plugins.AppMetricaPlugins;
import io.appmetrica.analytics.profile.UserProfile;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaTests extends CommonTest {

    @Mock
    private AppMetricaProxy mProxy;
    @Mock
    private Activity mActivity;
    @Mock
    private ECommerceEvent eCommerceEvent;
    private Context mContext;
    private AppMetricaProxy mActualProxy;
    private final String mEventName = "Some event name";
    private final String mEventValue = "Some event value";
    private final Map<String, Object> mAttributes = new HashMap<String, Object>();

    private static final String API_KEY = TestsData.UUID_API_KEY;

    @Rule
    public final MockedStaticRule<AppMetricaProxyProvider> sProxyProvider = new MockedStaticRule<>(AppMetricaProxyProvider.class);
    @Rule
    public final MockedStaticRule<AppMetricaPluginsImplProvider> sPluginsImplProvider =
            new MockedStaticRule<>(AppMetricaPluginsImplProvider.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        mAttributes.put("key", 20);
        when(AppMetricaProxyProvider.getProxy()).thenReturn(mProxy);
    }

    @Test
    public void testActivate() {
        AppMetricaConfig config = mock(AppMetricaConfig.class);
        AppMetrica.activate(mContext, config);
        verify(mProxy).activate(mContext, config);
    }

    @Test
    public void testSendEventsBuffer() {
        AppMetrica.sendEventsBuffer();
        verify(mProxy).sendEventsBuffer();
    }

    @Test
    public void testResumeSession() {
        AppMetrica.resumeSession(mActivity);
        verify(mProxy).resumeSession(mActivity);
    }

    @Test
    public void testPauseSession() {
        AppMetrica.pauseSession(mActivity);
        verify(mProxy).pauseSession(mActivity);
    }

    @Test
    public void testEnableActivityAutoTracking() {
        Application application = mock(Application.class);
        AppMetrica.enableActivityAutoTracking(application);
        verify(mProxy).enableActivityAutoTracking(application);
    }

    @Test
    public void testReportEvent() {
        AppMetrica.reportEvent(mEventName);
        verify(mProxy).reportEvent(mEventName);
    }

    @Test
    public void testReportError() {
        Throwable throwable = mock(Throwable.class);
        AppMetrica.reportError(mEventName, throwable);
        verify(mProxy).reportError(mEventName, throwable);
    }

    @Test
    public void testReportCustomError() {
        String id = "ididid";
        Throwable throwable = mock(Throwable.class);
        AppMetrica.reportError(id, mEventName, throwable);
        verify(mProxy).reportError(id, mEventName, throwable);
    }

    @Test
    public void testReportUnhandledException() {
        Throwable throwable = mock(Throwable.class);
        AppMetrica.reportUnhandledException(throwable);
        verify(mProxy).reportUnhandledException(throwable);
    }

    @Test
    public void testReportEventWithValue() {
        AppMetrica.reportEvent(mEventName, mEventValue);
        verify(mProxy).reportEvent(mEventName, mEventValue);
    }

    @Test
    public void testReportEventWIthAttributes() {
        AppMetrica.reportEvent(mEventName, mAttributes);
        verify(mProxy).reportEvent(mEventName, mAttributes);
    }

    @Test
    public void testReportAppOpen() {
        AppMetrica.reportAppOpen(mActivity);
        verify(mProxy).reportAppOpen(mActivity);
    }

    @Test
    public void testReportAppOpenIntent() {
        Intent intent = mock(Intent.class);
        AppMetrica.reportAppOpen(intent);
        verify(mProxy).reportAppOpen(intent);
    }

    @Test
    public void testReportAppOpenDeeplink() {
        final String deeplink = "deeplink";
        AppMetrica.reportAppOpen(deeplink);
        verify(mProxy).reportAppOpen(deeplink);
    }

    @Test
    public void testReportReferralUrl() {
        final String url = "referral url";
        AppMetrica.reportReferralUrl(url);
        verify(mProxy).reportReferralUrl(url);
    }

    @Test
    public void testSetLocation() {
        Location location = mock(Location.class);
        AppMetrica.setLocation(location);
        verify(mProxy).setLocation(location);
    }

    @Test
    public void testSetLocationTrackingEnabled() {
        final boolean enabled = new Random().nextBoolean();
        AppMetrica.setLocationTracking(enabled);
        verify(mProxy).setLocationTracking(enabled);
    }

    @Test
    public void testSetLocationTrackingEnabledWithContext() {
        final boolean enabled = new Random().nextBoolean();
        AppMetrica.setLocationTracking(mContext, enabled);
        verify(mProxy).setLocationTracking(mContext, enabled);
    }

    @Test
    public void testSetStatisticsSending() {
        final boolean enabled = new Random().nextBoolean();
        AppMetrica.setStatisticsSending(mContext, enabled);
        verify(mProxy).setStatisticsSending(mContext, enabled);
    }

    @Test
    public void testActivateReporter() {
        ReporterConfig reporterConfig = mock(ReporterConfig.class);
        AppMetrica.activateReporter(mContext, reporterConfig);
        verify(mProxy).activateReporter(mContext, reporterConfig);
    }

    @Test
    public void testGetReporter() {
        IReporterExtended reporter = mock(IReporterExtended.class);
        when(mProxy.getReporter(mContext, API_KEY)).thenReturn(reporter);
        assertThat(AppMetrica.getReporter(mContext, API_KEY)).isEqualTo(reporter);
    }

    @Test
    public void testGetLibraryVersion() {
        assertThat(AppMetrica.getLibraryVersion()).isEqualTo(BuildConfig.VERSION_NAME);
    }

    @Test
    public void testGetLibraryApiLevel() {
        assertThat(AppMetrica.getLibraryApiLevel()).isEqualTo(BuildConfig.API_LEVEL);
    }

    @Test
    public void testRequestDeferredDeeplinkParameters() {
        DeferredDeeplinkParametersListener listener = mock(DeferredDeeplinkParametersListener.class);
        AppMetrica.requestDeferredDeeplinkParameters(listener);
        verify(mProxy).requestDeferredDeeplinkParameters(listener);
    }

    @Test
    public void testRequestDeferredDeeplink() {
        DeferredDeeplinkListener listener = mock(DeferredDeeplinkListener.class);
        AppMetrica.requestDeferredDeeplink(listener);
        verify(mProxy).requestDeferredDeeplink(listener);
    }

    @Test
    public void testSetUserProfileId() {
        final String id = "some id";
        AppMetrica.setUserProfileID(id);
        verify(mProxy).setUserProfileID(id);
    }

    @Test
    public void testReportUserProfile() {
        UserProfile userProfile = mock(UserProfile.class);
        AppMetrica.reportUserProfile(userProfile);
        verify(mProxy).reportUserProfile(userProfile);
    }

    @Test
    public void testReportRevenue() {
        Revenue revenue = mock(Revenue.class);
        AppMetrica.reportRevenue(revenue);
        verify(mProxy).reportRevenue(revenue);
    }

    @Test
    public void testReportAdRevenue() {
        AdRevenue revenue = mock(AdRevenue.class);
        AppMetrica.reportAdRevenue(revenue);
        verify(mProxy).reportAdRevenue(revenue);
    }

    @Test
    public void testPutErrorEnvironmentValue() {
        String key = "key";
        String value = "value";
        AppMetrica.putErrorEnvironmentValue(key, value);
        verify(mProxy).putErrorEnvironmentValue(key, value);
    }

    @Test
    public void reportECommerce() {
        AppMetrica.reportECommerce(eCommerceEvent);
        verify(mProxy).reportECommerce(eCommerceEvent);
    }

    @Test
    public void initWebViewReporting() {
        WebView webView = mock(WebView.class);
        AppMetrica.initWebViewReporting(webView);
        verify(mProxy).initWebViewReporting(webView);
    }

    @Test
    public void putAppEnvironmentValue() {
        final String key = "appEnvironmentKey";
        final String value = "appEnvironmentValue";
        AppMetrica.putAppEnvironmentValue(key, value);
        verify(mProxy).putAppEnvironmentValue(key, value);
    }

    @Test
    public void clearAppEnvironment() {
        AppMetrica.clearAppEnvironment();
        verify(mProxy).clearAppEnvironment();
    }

    @Test
    public void getPluginExtension() {
        AppMetricaPlugins pluginsImpl = mock(AppMetricaPlugins.class);
        when(AppMetricaPluginsImplProvider.getImpl()).thenReturn(pluginsImpl);
        assertThat(AppMetrica.getPluginExtension()).isSameAs(pluginsImpl);
    }

    @Test
    public void getDeviceId() {
        final String deviceId = "888999777666";
        when(mProxy.getDeviceId()).thenReturn(deviceId);
        assertThat(AppMetrica.getDeviceId(mContext)).isEqualTo(deviceId);
    }

    @Test
    public void getUuid() {
        String uuid = UUID.randomUUID().toString();
        IdentifiersResult uuidResult = new IdentifiersResult(uuid, IdentifierStatus.OK, null);
        when(mProxy.getUuid(mContext)).thenReturn(uuidResult);
        assertThat(AppMetrica.getUuid(mContext)).isEqualTo(uuid);
    }

    @Test
    public void getUuidForNullValue() {
        IdentifiersResult identifiersResult = new IdentifiersResult(null, IdentifierStatus.OK, null);
        when(mProxy.getUuid(mContext)).thenReturn(identifiersResult);
        assertThat(AppMetrica.getUuid(mContext)).isNull();
    }

    @Test
    public void registerAnrListener() {
        AnrListener listener = mock(AnrListener.class);
        AppMetrica.registerAnrListener(listener);
        verify(mProxy).registerAnrListener(eq(listener));
    }
}
