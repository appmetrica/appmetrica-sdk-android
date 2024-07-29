package io.appmetrica.analytics.impl.selfreporting;

import android.content.Context;
import io.appmetrica.analytics.AdRevenue;
import io.appmetrica.analytics.ModuleEvent;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.SdkData;
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.impl.proxy.ReporterExtendedProxy;
import io.appmetrica.analytics.impl.proxy.ReporterProxyStorage;
import io.appmetrica.analytics.plugins.IPluginReporter;
import io.appmetrica.analytics.plugins.PluginErrorDetails;
import io.appmetrica.analytics.profile.UserProfile;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SelfReporterWrapperTest extends CommonTest {

    @Mock
    private ReporterProxyStorage mReporterProxyStorage;
    @Mock
    private ReporterExtendedProxy mReporter;
    @Mock
    private IPluginReporter pluginReporter;
    @Mock
    private ECommerceEvent eCommerceEvent;
    @Mock
    private PluginErrorDetails errorDetails;
    @Mock
    private AdRevenue adRevenue;

    private SelfReporterWrapper mSelfReporterWrapper;

    private Context mContext;

    @Rule
    public final MockedStaticRule<ReporterProxyStorage> sReporterProxyStorage = new MockedStaticRule<>(ReporterProxyStorage.class);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(ReporterProxyStorage.getInstance()).thenReturn(mReporterProxyStorage);
        mContext = RuntimeEnvironment.getApplication();
        when(mReporterProxyStorage.getOrCreate(mContext, SdkData.SDK_API_KEY_UUID)).thenReturn(mReporter);
        when(mReporter.getPluginExtension()).thenReturn(pluginReporter);
        mSelfReporterWrapper = new SelfReporterWrapper();
    }

    @Test
    public void testReportUnhandledExceptionThrowableInitialized() {
        Throwable exception = mock(Throwable.class);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportUnhandledException(exception);
        verify(mReporter).reportUnhandledException(exception);
    }

    @Test
    public void testReportUnhandledExceptionThrowableNotInitialized() {
        Throwable exception = mock(Throwable.class);
        mSelfReporterWrapper.reportUnhandledException(exception);
        verify(mReporter, never()).reportUnhandledException(exception);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportUnhandledException(exception);
    }

    @Test
    public void testReportUnhandledExceptionInitialized() {
        UnhandledException exception = mock(UnhandledException.class);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportUnhandledException(exception);
        verify(mReporter).reportUnhandledException(exception);
    }

    @Test
    public void testReportUnhandledExceptionNotInitialized() {
        UnhandledException exception = mock(UnhandledException.class);
        mSelfReporterWrapper.reportUnhandledException(exception);
        verify(mReporter, never()).reportUnhandledException(exception);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportUnhandledException(exception);
    }

    @Test
    public void testReportAnrInitialized() {
        AllThreads allThreads = mock(AllThreads.class);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportAnr(allThreads);
        verify(mReporter).reportAnr(allThreads);
    }

    @Test
    public void testReportAnrNotInitialized() {
        AllThreads allThreads = mock(AllThreads.class);
        mSelfReporterWrapper.reportAnr(allThreads);
        verify(mReporter, never()).reportAnr(allThreads);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportAnr(allThreads);
    }

    @Test
    public void testPutAppEnvironmentValueInitialized() {
        final String key = "aaa";
        final String value = "bbb";
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.putAppEnvironmentValue(key, value);
        verify(mReporter).putAppEnvironmentValue(key, value);
    }

    @Test
    public void testPutAppEnvironmentValueNotInitialized() {
        final String key = "aaa";
        final String value = "bbb";
        mSelfReporterWrapper.putAppEnvironmentValue(key, value);
        verify(mReporter, never()).putAppEnvironmentValue(key, value);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).putAppEnvironmentValue(key, value);
    }

    @Test
    public void testClearAppEnvironmentInitialized() {
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.clearAppEnvironment();
        verify(mReporter).clearAppEnvironment();
    }

    @Test
    public void testClearAppEnvironmentNotInitialized() {
        mSelfReporterWrapper.clearAppEnvironment();
        verify(mReporter, never()).clearAppEnvironment();
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).clearAppEnvironment();
    }

    @Test
    public void testSendEventsBufferInitialized() {
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.sendEventsBuffer();
        verify(mReporter).sendEventsBuffer();
    }

    @Test
    public void testSendEventsBufferNotInitialized() {
        mSelfReporterWrapper.sendEventsBuffer();
        verify(mReporter, never()).sendEventsBuffer();
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).sendEventsBuffer();
    }

    @Test
    public void testReportEventInitialized() {
        final String name = "event name";
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportEvent(name);
        verify(mReporter).reportEvent(name);
    }

    @Test
    public void testReportEventNotInitialized() {
        final String name = "event name";
        mSelfReporterWrapper.reportEvent(name);
        verify(mReporter, never()).reportEvent(name);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportEvent(name);
    }

    @Test
    public void testReportEventValueInitialized() {
        final String name = "name";
        final String value = "value";
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportEvent(name, value);
        verify(mReporter).reportEvent(name, value);
    }

    @Test
    public void testReportEventValueNotInitialized() {
        final String name = "name";
        final String value = "value";
        mSelfReporterWrapper.reportEvent(name, value);
        verify(mReporter, never()).reportEvent(name, value);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportEvent(name, value);
    }

    @Test
    public void testReportEventAttributesInitialized() {
        final String name = "name";
        final Map<String, Object> attributes = mock(Map.class);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportEvent(name, attributes);
        verify(mReporter).reportEvent(name, attributes);
    }

    @Test
    public void testReportEventAttributesNotInitialized() {
        final String name = "name";
        final Map<String, Object> attributes = mock(Map.class);
        mSelfReporterWrapper.reportEvent(name, attributes);
        verify(mReporter, never()).reportEvent(name, attributes);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportEvent(name, attributes);
    }

    @Test
    public void testReportErrorInitialized() {
        Throwable throwable = mock(Throwable.class);
        final String name = "event name";
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportError(name, throwable);
        verify(mReporter).reportError(name, throwable);
    }

    @Test
    public void testReportErrorNotInitialized() {
        Throwable throwable = mock(Throwable.class);
        final String name = "event name";
        mSelfReporterWrapper.reportError(name, throwable);
        verify(mReporter, never()).reportError(name, throwable);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportError(name, throwable);
    }

    @Test
    public void testReportCustomWithoutThrowableErrorInitialized() {
        String id = "id";
        final String name = "event name";
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportError(id, name);
        verify(mReporter).reportError(id, name, null);
    }

    @Test
    public void testReportCustomWithoutThrowableErrorNotInitialized() {
        String id = "id";
        final String name = "event name";
        mSelfReporterWrapper.reportError(id, name);
        verify(mReporter, never()).reportError(id, name, null);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportError(id, name, null);
    }

    @Test
    public void testReportCustomErrorInitialized() {
        Throwable throwable = mock(Throwable.class);
        String id = "id";
        final String name = "event name";
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportError(id, name, throwable);
        verify(mReporter).reportError(id, name, throwable);
    }

    @Test
    public void testReportCustomErrorNotInitialized() {
        Throwable throwable = mock(Throwable.class);
        String id = "id";
        final String name = "event name";
        mSelfReporterWrapper.reportError(name, throwable);
        verify(mReporter, never()).reportError(id, name, throwable);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportError(name, throwable);
    }

    @Test
    public void testResumeSessionInitialized() {
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.resumeSession();
        verify(mReporter).resumeSession();
    }

    @Test
    public void testResumeSessionNotInitialized() {
        mSelfReporterWrapper.resumeSession();
        verify(mReporter, never()).resumeSession();
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).resumeSession();
    }

    @Test
    public void testPauseSessionInitialized() {
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.pauseSession();
        verify(mReporter).pauseSession();
    }

    @Test
    public void testPauseSessionNotInitialized() {
        mSelfReporterWrapper.pauseSession();
        verify(mReporter, never()).pauseSession();
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).pauseSession();
    }

    @Test
    public void testSetUserProfileIdInitialized() {
        final String userProfileId = "1234567890";
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.setUserProfileID(userProfileId);
        verify(mReporter).setUserProfileID(userProfileId);
    }

    @Test
    public void testSetUserProfileIdNotInitialized() {
        final String userProfileId = "1234567890";
        mSelfReporterWrapper.setUserProfileID(userProfileId);
        verify(mReporter, never()).setUserProfileID(userProfileId);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).setUserProfileID(userProfileId);
    }

    @Test
    public void testReportUserProfileInitialized() {
        UserProfile userProfile = mock(UserProfile.class);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportUserProfile(userProfile);
        verify(mReporter).reportUserProfile(userProfile);
    }

    @Test
    public void testReportUserProfileNotInitialized() {
        UserProfile userProfile = mock(UserProfile.class);
        mSelfReporterWrapper.reportUserProfile(userProfile);
        verify(mReporter, never()).reportUserProfile(userProfile);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportUserProfile(userProfile);
    }

    @Test
    public void testReportRevenueInitialized() {
        Revenue revenue = mock(Revenue.class);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportRevenue(revenue);
        verify(mReporter).reportRevenue(revenue);
    }

    @Test
    public void testReportRevenueNotInitialized() {
        Revenue revenue = mock(Revenue.class);
        mSelfReporterWrapper.reportRevenue(revenue);
        verify(mReporter, never()).reportRevenue(revenue);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportRevenue(revenue);
    }

    @Test
    public void reportECommerceEvent() {
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportECommerce(eCommerceEvent);
        verify(mReporter).reportECommerce(eCommerceEvent);
    }

    @Test
    public void reportECommerceBeforeInitialization() {
        mSelfReporterWrapper.reportECommerce(eCommerceEvent);
        verify(mReporter, never()).reportECommerce(eCommerceEvent);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportECommerce(eCommerceEvent);
    }

    @Test
    public void dataSendingEnabledInitialized() {
        final boolean enabled = true;
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.setDataSendingEnabled(enabled);
        verify(mReporter).setDataSendingEnabled(enabled);
    }

    @Test
    public void dataSendingEnabledNotInitialized() {
        final boolean enabled = true;
        mSelfReporterWrapper.setDataSendingEnabled(enabled);
        verify(mReporter, never()).setDataSendingEnabled(enabled);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).setDataSendingEnabled(enabled);
    }

    @Test
    public void reportPluginUnhandledException() {
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportUnhandledException(errorDetails);
        verify(pluginReporter).reportUnhandledException(errorDetails);
    }

    @Test
    public void reportPluginUnhandledExceptionBeforeInitialization() {
        mSelfReporterWrapper.reportUnhandledException(errorDetails);
        verify(pluginReporter, never()).reportUnhandledException(errorDetails);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(pluginReporter).reportUnhandledException(errorDetails);
    }

    @Test
    public void reportPluginError() {
        String message = "some message";
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportError(errorDetails, message);
        verify(pluginReporter).reportError(errorDetails, message);
    }

    @Test
    public void reportPluginErrorBeforeInitialization() {
        String message = "some message";
        mSelfReporterWrapper.reportError(errorDetails, message);
        verify(pluginReporter, never()).reportError(errorDetails, message);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(pluginReporter).reportError(errorDetails, message);
    }

    @Test
    public void reportPluginErrorWithIdentifier() {
        String id = "some id";
        String message = "some message";
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportError(id, message, errorDetails);
        verify(pluginReporter).reportError(id, message, errorDetails);
    }

    @Test
    public void reportPluginErrorWithIdentifierBeforeInitialization() {
        String id = "some id";
        String message = "some message";
        mSelfReporterWrapper.reportError(id, message, errorDetails);
        verify(pluginReporter, never()).reportError(id, message, errorDetails);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(pluginReporter).reportError(id, message, errorDetails);
    }

    @Test
    public void reportAdRevenue() {
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportAdRevenue(adRevenue);
        verify(mReporter).reportAdRevenue(adRevenue);
    }

    @Test
    public void reportAdRevenueNotInitialized() {
        mSelfReporterWrapper.reportAdRevenue(adRevenue);
        verify(mReporter, never()).reportAdRevenue(adRevenue);
        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportAdRevenue(adRevenue);
    }

    @Test
    public void reportCustomEventInitialized() {
        mSelfReporterWrapper.onInitializationFinished(mContext);

        final ModuleEvent moduleEvent = mock(ModuleEvent.class);

        mSelfReporterWrapper.reportEvent(moduleEvent);

        verify(mReporter).reportEvent(moduleEvent);
    }

    @Test
    public void reportCustomEventNotInitialized() {
        final ModuleEvent moduleEvent = mock(ModuleEvent.class);

        mSelfReporterWrapper.reportEvent(moduleEvent);
        verify(mReporter, never()).reportEvent(moduleEvent);

        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportEvent(moduleEvent);
    }

    @Test
    public void setSessionExtraInitialized() {
        String key = "Key";
        byte[] value = new byte[]{1, 6, 3};
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.setSessionExtra(key, value);
        verify(mReporter).setSessionExtra(key, value);
    }

    @Test
    public void setSessionExtraNotInitialized() {
        String key = "Key";
        byte[] value = new byte[]{1, 6, 3};

        mSelfReporterWrapper.setSessionExtra(key, value);
        verify(mReporter, never()).setSessionExtra(key, value);

        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).setSessionExtra(key, value);
    }

    @Test
    public void reportAdRevenueWithAutoCollectedInitialized() {
        mSelfReporterWrapper.onInitializationFinished(mContext);
        mSelfReporterWrapper.reportAdRevenue(adRevenue, true);
        verify(mReporter).reportAdRevenue(adRevenue, true);
    }

    @Test
    public void reportAdRevenueWithAutoCollectedNotInitialized() {
        mSelfReporterWrapper.reportAdRevenue(adRevenue, true);
        verify(mReporter, never()).reportAdRevenue(adRevenue, true);

        mSelfReporterWrapper.onInitializationFinished(mContext);
        verify(mReporter).reportAdRevenue(adRevenue, true);
    }
}
