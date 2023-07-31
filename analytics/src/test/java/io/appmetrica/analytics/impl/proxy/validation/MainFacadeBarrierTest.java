package io.appmetrica.analytics.impl.proxy.validation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.webkit.WebView;
import io.appmetrica.analytics.AnrListener;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.ValidationException;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class MainFacadeBarrierTest extends CommonTest {

    private MainFacadeBarrier mBarrier = new MainFacadeBarrier();

    @Test
    public void testEnableAutoTracking() {
        mBarrier.enableActivityAutoTracking(RuntimeEnvironment.getApplication());
    }

    @Test(expected = ValidationException.class)
    public void testEnableAutoTrackingWithNull() {
        mBarrier.enableActivityAutoTracking(null);
    }

    @Test
    public void testAppOpenActivity() {
        mBarrier.reportAppOpen(mock(Activity.class));
    }

    @Test(expected = ValidationException.class)
    public void testAppOpenActivityNull() {
        mBarrier.reportAppOpen((Activity) null);
    }

    @Test
    public void testAppOpenIntent() {
        mBarrier.reportAppOpen(mock(Intent.class));
    }

    @Test(expected = ValidationException.class)
    public void testAppOpenIntentNull() {
        mBarrier.reportAppOpen((Intent) null);
    }

    @Test
    public void testAppOpenString() {
        mBarrier.reportAppOpen("deeplink");
    }

    @Test(expected = ValidationException.class)
    public void testAppOpenStringEmpty() {
        mBarrier.reportAppOpen("");
    }

    @Test(expected = ValidationException.class)
    public void testAppOpenStringNull() {
        mBarrier.reportAppOpen((String) null);
    }

    @Test
    public void testReferralUrlString() {
        mBarrier.reportReferralUrl("ReferralUrl");
    }

    @Test(expected = ValidationException.class)
    public void testReferralUrlStringEmpty() {
        mBarrier.reportReferralUrl("");
    }

    @Test(expected = ValidationException.class)
    public void testReferralUrlStringNull() {
        mBarrier.reportReferralUrl(null);
    }

    @Test
    public void testLocationTracking() {
        mBarrier.setLocationTracking(RuntimeEnvironment.getApplication(), false);
    }

    @Test(expected = ValidationException.class)
    public void testLocationTrackingNullContext() {
        mBarrier.setLocationTracking(null, false);
    }

    @Test
    public void testDeeplinkParametersListener() {
        mBarrier.requestDeferredDeeplinkParameters(mock(DeferredDeeplinkParametersListener.class));
    }

    @Test(expected = ValidationException.class)
    public void testDeeplinkParametersListenerNull() {
        mBarrier.requestDeferredDeeplinkParameters(null);
    }

    @Test
    public void testDeeplinkListener() {
        mBarrier.requestDeferredDeeplink(mock(DeferredDeeplinkListener.class));
    }

    @Test(expected = ValidationException.class)
    public void testDeeplinkListenerNull() {
        mBarrier.requestDeferredDeeplink(null);
    }

    @Test
    public void testStatisticsSending() {
        mBarrier.setStatisticsSending(RuntimeEnvironment.getApplication(), false);
    }

    @Test(expected = ValidationException.class)
    public void testStatisticsSendingNullContext() {
        mBarrier.setStatisticsSending(null, false);
    }

    @Test
    public void testGetReporter() {
        mBarrier.getReporter(RuntimeEnvironment.getApplication(), UUID.randomUUID().toString());
    }

    @Test(expected = ValidationException.class)
    public void testGetReporterContextNull() {
        mBarrier.getReporter(null, "apiKey");
    }

    @Test(expected = ValidationException.class)
    public void testGetReporterApiKeyInvalid() {
        mBarrier.getReporter(RuntimeEnvironment.getApplication(), "apiKey");
    }

    @Test(expected = ValidationException.class)
    public void testGetReporterApiKeyEmpty() {
        mBarrier.getReporter(RuntimeEnvironment.getApplication(), "");
    }

    @Test(expected = ValidationException.class)
    public void testGetReporterApiKeyNull() {
        mBarrier.getReporter(RuntimeEnvironment.getApplication(), null);
    }

    @Test
    public void testActivateReporter() {
        mBarrier.activateReporter(RuntimeEnvironment.getApplication(), mock(ReporterConfig.class));
    }

    @Test(expected = ValidationException.class)
    public void testActivateReporterContextNull() {
        mBarrier.activateReporter(null, mock(ReporterConfig.class));
    }

    @Test(expected = ValidationException.class)
    public void testActivateReporterConfigNull() {
        mBarrier.activateReporter(RuntimeEnvironment.getApplication(), null);
    }

    @Test
    public void testActivate() {
        mBarrier.activate(RuntimeEnvironment.getApplication(), AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString()).build());
    }

    @Test(expected = ValidationException.class)
    public void testActivateNullContext() {
        mBarrier.activate(null, AppMetricaConfig.newConfigBuilder(UUID.randomUUID().toString()).build());
    }

    @Test(expected = ValidationException.class)
    public void testActivateNullConfig() {
        mBarrier.activate(RuntimeEnvironment.getApplication(), null);
    }

    @Test
    public void testPuErrorEnvironmentValue() {
        mBarrier.putErrorEnvironmentValue("key", "value");
    }

    @Test
    public void testPuErrorEnvironmentValueValueIsNull() {
        mBarrier.putErrorEnvironmentValue("key", null);
    }

    @Test(expected = ValidationException.class)
    public void testPuErrorEnvironmentValueKeyIsNull() {
        mBarrier.putErrorEnvironmentValue(null, "value");
    }

    @Test
    public void initWebViewReporting() {
        mBarrier.initWebViewReporting(mock(WebView.class));
    }

    @Test(expected = ValidationException.class)
    public void initWebViewReportingNull() {
        mBarrier.initWebViewReporting(null);
    }

    @Test
    public void reportJsEventAllFilled() {
        assertThat(mBarrier.reportJsEvent("name", "value")).isTrue();
    }

    @Test
    public void reportJsEventNullValue() {
        assertThat(mBarrier.reportJsEvent("name", null)).isTrue();
    }

    @Test
    public void reportJsEventNullName() {
        assertThat(mBarrier.reportJsEvent(null, "value")).isFalse();
    }

    @Test
    public void reportJsEventEmptyName() {
        assertThat(mBarrier.reportJsEvent("", "value")).isFalse();
    }

    @Test
    public void reportJsInitEventFilled() {
        assertThat(mBarrier.reportJsInitEvent("aaa")).isTrue();
    }

    @Test
    public void reportJsInitEventEmpty() {
        assertThat(mBarrier.reportJsInitEvent("")).isFalse();
    }

    @Test
    public void reportJsInitEventNull() {
        assertThat(mBarrier.reportJsInitEvent(null)).isFalse();
    }

    @Test
    public void testRequestStartupParams() {
        mBarrier.requestStartupParams(RuntimeEnvironment.getApplication(), mock(StartupParamsCallback.class), mock(List.class));
    }

    @Test(expected = ValidationException.class)
    public void testRequestStartupParamsNullContext() {
        mBarrier.requestStartupParams(null, mock(StartupParamsCallback.class), mock(List.class));
    }

    @Test(expected = ValidationException.class)
    public void testRequestStartupParamsNullCallback() {
        mBarrier.requestStartupParams(RuntimeEnvironment.getApplication(), null, mock(List.class));
    }

    @Test
    public void testRequestStartupParamsNullParams() {
        mBarrier.requestStartupParams(RuntimeEnvironment.getApplication(), mock(StartupParamsCallback.class), null);
    }

    @Test(expected = ValidationException.class)
    public void getUuidForNullContext() {
        mBarrier.getUuid(null);
    }

    @Test
    public void getUuidForNonNullContext() {
        mBarrier.getUuid(mock(Context.class));
    }

    @Test
    public void registerAnrListener() {
        mBarrier.registerAnrListener(mock(AnrListener.class));
    }

    @Test(expected = ValidationException.class)
    public void registerAnrListenerNullListener() {
        mBarrier.registerAnrListener(null);
    }
}
