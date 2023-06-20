package io.appmetrica.analytics.impl.stub;

import android.app.Activity;
import android.location.Location;
import io.appmetrica.analytics.ModuleEvent;
import io.appmetrica.analytics.impl.ActivityLifecycleManager;
import io.appmetrica.analytics.impl.WebViewJsInterfaceHandler;
import io.appmetrica.analytics.plugins.PluginErrorDetails;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
public class MainReporterStubTest extends ReporterExtendedStubTest {

    @Mock
    private Activity activity;
    @Mock
    private Location location;
    @Mock
    private WebViewJsInterfaceHandler webViewJsInterfaceHandler;
    @Mock
    private PluginErrorDetails pluginErrorDetails;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void enableAnrMonitoring() {
        getStub().enableAnrMonitoring();
    }

    @Test
    public void reportAppOpen() {
        getStub().reportAppOpen("deeplink", true);
    }

    @Test
    public void reportReferralUrl() {
        getStub().reportReferralUrl("Referral Url");
    }

    @Test
    public void onEnableAutoTrackingAttemptOccurred() {
        getStub().onEnableAutoTrackingAttemptOccurred(ActivityLifecycleManager.WatchingStatus.WATCHING);
    }

    @Test
    public void resumeSessionWithActivity() {
        getStub().resumeSession(activity);
        verifyZeroInteractions(activity);
    }

    @Test
    public void pauseSessionWithActivity() {
        getStub().pauseSession(activity);
        verifyZeroInteractions(activity);
    }

    @Test
    public void getCustomHosts() {
        assertThat(getStub().getCustomHosts()).isEmpty();
    }

    @Test
    public void reportCustomEvent() {
        getStub().reportEvent(mock(ModuleEvent.class));
    }

    @Test
    public void isPaused() {
        assertThat(getStub().isPaused()).isFalse();
    }

    @Test
    public void setLocation() {
        getStub().setLocation(location);
        verifyZeroInteractions(location);
    }

    @Test
    public void setLocationTracking() {
        getStub().setLocationTracking(true);
    }

    @Test
    public void putErrorEnvironment() {
        getStub().putErrorEnvironmentValue("key", "value");
    }

    @Test
    public void reportJsEvent() {
        getStub().reportJsEvent("Event name", "Json value");
    }

    @Test
    public void reportJsInitEvent() {
        getStub().reportJsInitEvent("Value");
    }

    @Test
    public void onWebViewReportingInit() {
        getStub().onWebViewReportingInit(webViewJsInterfaceHandler);
        verifyZeroInteractions(webViewJsInterfaceHandler);
    }

    @Test
    public void reportPluginUnhandledException() {
        getStub().reportUnhandledException(pluginErrorDetails);
        verifyNoInteractions(pluginErrorDetails);
    }

    @Test
    public void reportPluginError() {
        getStub().reportError(pluginErrorDetails, "message");
        verifyNoInteractions(pluginErrorDetails);
    }

    @Test
    public void reportPluginErrorWithIdentifier() {
        getStub().reportError("id", "message", pluginErrorDetails);
        verifyNoInteractions(pluginErrorDetails);
    }

    @Override
    public MainReporterStub getStub() {
        return new MainReporterStub();
    }
}
