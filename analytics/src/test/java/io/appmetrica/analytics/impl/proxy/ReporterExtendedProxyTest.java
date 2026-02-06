package io.appmetrica.analytics.impl.proxy;

import android.content.Context;
import io.appmetrica.analytics.AdRevenue;
import io.appmetrica.analytics.ModuleEvent;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.Revenue;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.ecommerce.ECommerceEvent;
import io.appmetrica.analytics.impl.AppMetricaFacade;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.IReporterExtended;
import io.appmetrica.analytics.impl.TestsData;
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.impl.proxy.synchronous.ReporterSynchronousStageExecutor;
import io.appmetrica.analytics.impl.proxy.validation.ReporterBarrier;
import io.appmetrica.analytics.profile.UserProfile;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.ContextRule;
import io.appmetrica.analytics.testutils.MockProvider;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReporterExtendedProxyTest extends CommonTest {

    @Mock
    private ReporterBarrier reporterBarrier;
    @Mock
    private AppMetricaFacadeProvider mProvider;
    @Mock
    private AppMetricaFacade mAppMetricaFacade;
    @Mock
    private IReporterExtended mReporter;
    @Mock
    private ReporterSynchronousStageExecutor mSynchronousStageExecutor;
    @Mock
    private ECommerceEvent eCommerceEvent;
    @Mock
    private PluginReporterProxy pluginReporterProxy;

    @Rule
    public ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();
    
    @Rule
    public ContextRule contextRule = new ContextRule();

    private final String mApiKey = TestsData.generateApiKey();
    private ReporterExtendedProxy mReporterExtendedProxy;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        IHandlerExecutor executor = MockProvider.mockedBlockingExecutorMock();
        when(ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor())
            .thenReturn(executor);
        when(mProvider.getInitializedImpl(any(Context.class))).thenReturn(mAppMetricaFacade);
        when(mAppMetricaFacade.getReporter(any(ReporterConfig.class))).thenReturn(mReporter);
        mReporterExtendedProxy = new ReporterExtendedProxy(
            contextRule.getContext(),
            reporterBarrier,
            mProvider,
            mSynchronousStageExecutor,
            ReporterConfig.newConfigBuilder(mApiKey).build(),
            pluginReporterProxy
        );
    }

    @Test
    public void testReportUnhandledException() {
        UnhandledException unhandledException = mock(UnhandledException.class);
        mReporterExtendedProxy.reportUnhandledException(unhandledException);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportUnhandledException(unhandledException);
        inOrder.verify(mSynchronousStageExecutor).reportUnhandledException(unhandledException);
        inOrder.verify(mReporter).reportUnhandledException(unhandledException);
    }

    @Test
    public void testReportAnr() {
        AllThreads allThreads = mock(AllThreads.class);
        mReporterExtendedProxy.reportAnr(allThreads);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportAnr(allThreads);
        inOrder.verify(mSynchronousStageExecutor).reportAnr(allThreads);
        inOrder.verify(mReporter).reportAnr(allThreads);
    }

    @Test
    public void reportAnrFromApi() {
        Thread thread = mock(Thread.class);
        StackTraceElement[] stackTraceElements = new StackTraceElement[]{mock(StackTraceElement.class)};
        Map<Thread, StackTraceElement[]> allThreads = new HashMap<>();
        allThreads.put(thread, stackTraceElements);
        //noinspection unchecked
        ArgumentCaptor<Map<Thread, StackTraceElement[]>> allThreadsCaptor = ArgumentCaptor.forClass(Map.class);
        mReporterExtendedProxy.reportAnr(allThreads);

        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportAnr(allThreads);
        inOrder.verify(mSynchronousStageExecutor).reportAnr(allThreads);
        inOrder.verify(mReporter).reportAnr(allThreadsCaptor.capture());
        inOrder.verifyNoMoreInteractions();

        assertThat(allThreadsCaptor.getValue())
            .isNotSameAs(allThreads)
            .containsExactlyEntriesOf(allThreads);
    }

    @Test
    public void testSendEventsBuffer() {
        mReporterExtendedProxy.sendEventsBuffer();
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).sendEventsBuffer();
        inOrder.verify(mSynchronousStageExecutor).sendEventsBuffer();
        inOrder.verify(mReporter).sendEventsBuffer();
    }

    @Test
    public void testPutAppEnvironmentValue() {
        final String key = "key";
        final String value = "value";
        mReporterExtendedProxy.putAppEnvironmentValue(key, value);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).putAppEnvironmentValue(key, value);
        inOrder.verify(mSynchronousStageExecutor).putAppEnvironmentValue(key, value);
        inOrder.verify(mReporter).putAppEnvironmentValue(key, value);
    }

    @Test
    public void testClearAppEnvironment() {
        mReporterExtendedProxy.clearAppEnvironment();
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).clearAppEnvironment();
        inOrder.verify(mSynchronousStageExecutor).clearAppEnvironment();
        inOrder.verify(mReporter).clearAppEnvironment();
    }

    @Test
    public void testReportEvent() {
        final String name = "name";
        mReporterExtendedProxy.reportEvent(name);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportEvent(name);
        inOrder.verify(mSynchronousStageExecutor).reportEvent(name);
        inOrder.verify(mReporter).reportEvent(name);
    }

    @Test
    public void testReportEventValue() {
        final String name = "name";
        final String data = "data";
        mReporterExtendedProxy.reportEvent(name, data);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportEvent(name, data);
        inOrder.verify(mSynchronousStageExecutor).reportEvent(name, data);
        inOrder.verify(mReporter).reportEvent(name, data);
    }

    @Test
    public void testReportEventAttributes() {
        final String name = "name";
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("key1", 11);
        attributes.put("key2", "value");
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        mReporterExtendedProxy.reportEvent(name, attributes);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportEvent(name, attributes);
        inOrder.verify(mSynchronousStageExecutor).reportEvent(name, attributes);
        inOrder.verify(mReporter).reportEvent(same(name), captor.capture());
        assertThat(captor.getValue()).containsAllEntriesOf(attributes).hasSameSizeAs(attributes);
    }

    @Test
    public void testReportEventAttributesArrayMap() {
        final String name = "name";
        Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("key1", 11);
        attributes.put("key2", "value");
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        mReporterExtendedProxy.reportEvent(name, attributes);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportEvent(name, attributes);
        inOrder.verify(mSynchronousStageExecutor).reportEvent(name, attributes);
        inOrder.verify(mReporter).reportEvent(same(name), captor.capture());
        assertThat(captor.getValue()).containsEntry("key1", 11).containsEntry("key2", "value").hasSize(2);
    }

    @Test
    public void testReportEventWithAttributesMapChanged() {
        IHandlerExecutor executor = mock(IHandlerExecutor.class);
        when(ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor())
            .thenReturn(executor);
        mReporterExtendedProxy = createProxyWithMockedExecutor();
        doNothing().when(executor).execute(any(Runnable.class));
        doReturn(true).when(mProvider).isActivated();
        final String name = "name";
        final Map<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("k1", "v1");
        mReporterExtendedProxy.reportEvent(name, attributes);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(runnableCaptor.capture());
        attributes.put("k1", "v2");
        runnableCaptor.getValue().run();
        verify(mReporter).reportEvent(eq(name), argThat(new ArgumentMatcher<Map<String, Object>>() {
            @Override
            public boolean matches(Map<String, Object> argument) {
                return argument.get("k1").equals("v1") && argument.size() == 1;
            }
        }));
    }

    @Test
    public void testReportEventAttributesNull() {
        final String name = "name";
        Map<String, Object> attributes = null;
        mReporterExtendedProxy.reportEvent(name, attributes);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportEvent(name, attributes);
        inOrder.verify(mSynchronousStageExecutor).reportEvent(name, attributes);
        inOrder.verify(mReporter).reportEvent(same(name), argThat(new ArgumentMatcher<Map>() {
            @Override
            public boolean matches(Map argument) {
                return argument.isEmpty();
            }
        }));
    }

    @Test
    public void testReportError() {
        final String name = "name";
        Throwable throwable = mock(Throwable.class);
        Throwable nonNullThrowable = mock(Throwable.class);
        when(mSynchronousStageExecutor.reportError(name, throwable)).thenReturn(nonNullThrowable);
        mReporterExtendedProxy.reportError(name, throwable);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportError(name, throwable);
        inOrder.verify(mSynchronousStageExecutor).reportError(name, throwable);
        inOrder.verify(mReporter).reportError(name, nonNullThrowable);
    }

    @Test
    public void testReportCustomErrorWithoutThrowable() {
        final String name = "name";
        String id = "ididid";
        Throwable throwable = mock(Throwable.class);
        mReporterExtendedProxy.reportError(id, name);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportError(id, name, null);
        inOrder.verify(mSynchronousStageExecutor).reportError(id, name, null);
        inOrder.verify(mReporter).reportError(id, name, null);
    }

    @Test
    public void testReportCustomError() {
        final String name = "name";
        String id = "ididid";
        Throwable throwable = mock(Throwable.class);
        mReporterExtendedProxy.reportError(id, name, throwable);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportError(id, name, throwable);
        inOrder.verify(mSynchronousStageExecutor).reportError(id, name, throwable);
        inOrder.verify(mReporter).reportError(id, name, throwable);
    }

    @Test
    public void testReportUnhandledExceptionThrowable() {
        Throwable throwable = mock(Throwable.class);
        mReporterExtendedProxy.reportUnhandledException(throwable);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportUnhandledException(throwable);
        inOrder.verify(mSynchronousStageExecutor).reportUnhandledException(throwable);
        inOrder.verify(mReporter).reportUnhandledException(throwable);
    }

    @Test
    public void testResumeSession() {
        mReporterExtendedProxy.resumeSession();
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).resumeSession();
        inOrder.verify(mSynchronousStageExecutor).resumeSession();
        inOrder.verify(mReporter).resumeSession();
    }

    @Test
    public void testPauseSession() {
        mReporterExtendedProxy.pauseSession();
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).pauseSession();
        inOrder.verify(mSynchronousStageExecutor).pauseSession();
        inOrder.verify(mReporter).pauseSession();
    }

    @Test
    public void testSetUserProfileId() {
        final String userProfileId = "user profile id";
        mReporterExtendedProxy.setUserProfileID(userProfileId);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).setUserProfileID(userProfileId);
        inOrder.verify(mSynchronousStageExecutor).setUserProfileID(userProfileId);
        inOrder.verify(mReporter).setUserProfileID(userProfileId);
    }

    @Test
    public void testReportUserProfile() {
        UserProfile userProfile = mock(UserProfile.class);
        mReporterExtendedProxy.reportUserProfile(userProfile);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportUserProfile(userProfile);
        inOrder.verify(mSynchronousStageExecutor).reportUserProfile(userProfile);
        inOrder.verify(mReporter).reportUserProfile(userProfile);
    }

    @Test
    public void testReportRevenue() {
        Revenue revenue = mock(Revenue.class);
        mReporterExtendedProxy.reportRevenue(revenue);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportRevenue(revenue);
        inOrder.verify(mSynchronousStageExecutor).reportRevenue(revenue);
        inOrder.verify(mReporter).reportRevenue(revenue);
    }

    @Test
    public void testReportAdRevenue() {
        AdRevenue revenue = mock(AdRevenue.class);
        mReporterExtendedProxy.reportAdRevenue(revenue);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportAdRevenue(revenue);
        inOrder.verify(mSynchronousStageExecutor).reportAdRevenue(revenue);
        inOrder.verify(mReporter).reportAdRevenue(revenue);
    }

    @Test
    public void reportECommerce() {
        mReporterExtendedProxy.reportECommerce(eCommerceEvent);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportECommerce(eCommerceEvent);
        inOrder.verify(mSynchronousStageExecutor).reportECommerce(eCommerceEvent);
        inOrder.verify(mReporter).reportECommerce(eCommerceEvent);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void setDataSendingEnabled() {
        final boolean enabled = true;
        mReporterExtendedProxy.setDataSendingEnabled(enabled);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).setDataSendingEnabled(enabled);
        inOrder.verify(mSynchronousStageExecutor).setDataSendingEnabled(enabled);
        inOrder.verify(mReporter).setDataSendingEnabled(enabled);
    }

    @Test
    public void testGetReporter() {
        ReporterExtendedProxy proxy = mock(ReporterExtendedProxy.class);
        when(mAppMetricaFacade.getReporter(argThat(new ArgumentMatcher<ReporterConfig>() {
            @Override
            public boolean matches(ReporterConfig argument) {
                return argument.apiKey.equals(mApiKey);
            }
        }))).thenReturn(proxy);
        assertThat(mReporterExtendedProxy.getReporter()).isEqualTo(proxy);
    }

    @Test
    public void testActivateApiKey() {
        ArgumentMatcher<ReporterConfig> configWithApiKey = new ArgumentMatcher<ReporterConfig>() {
            @Override
            public boolean matches(ReporterConfig argument) {
                return argument.apiKey.equals(mApiKey);
            }
        };
        mReporterExtendedProxy.activate(mApiKey);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mAppMetricaFacade);
        inOrder.verify(reporterBarrier).activate(argThat(configWithApiKey));
        inOrder.verify(mSynchronousStageExecutor).activate(argThat(configWithApiKey));
        inOrder.verify(mAppMetricaFacade).activateReporter(argThat(configWithApiKey));
    }

    @Test
    public void testActivateWithConfig() {
        ReporterConfig originalConfig = mock(ReporterConfig.class);
        mReporterExtendedProxy.activate(originalConfig);
        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mAppMetricaFacade);
        inOrder.verify(reporterBarrier).activate(originalConfig);
        inOrder.verify(mSynchronousStageExecutor).activate(originalConfig);
        inOrder.verify(mAppMetricaFacade).activateReporter(originalConfig);
    }

    @Test
    public void getPluginExtension() {
        assertThat(mReporterExtendedProxy.getPluginExtension()).isSameAs(pluginReporterProxy);
    }

    @Test
    public void reportCustomEvent() {
        final ModuleEvent moduleEvent = mock(ModuleEvent.class);
        mReporterExtendedProxy.reportEvent(moduleEvent);
        InOrder inOrder = Mockito.inOrder(mReporter);
        inOrder.verify(mReporter).reportEvent(moduleEvent);
    }

    @Test
    public void setSessionExtra() {
        String key = "Key";
        byte[] value = new byte[]{2, 5, 7};
        mReporterExtendedProxy.setSessionExtra(key, value);

        InOrder inOrder = Mockito.inOrder(mSynchronousStageExecutor, mReporter);
        inOrder.verify(mReporter).setSessionExtra(key, value);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void reportAdRevenueIfAutoCollected() {
        AdRevenue adRevenue = mock(AdRevenue.class);
        boolean autoCollected = true;
        mReporterExtendedProxy.reportAdRevenue(adRevenue, autoCollected);

        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportAdRevenue(adRevenue, autoCollected);
        inOrder.verify(mSynchronousStageExecutor).reportAdRevenue(adRevenue, autoCollected);
        inOrder.verify(mReporter).reportAdRevenue(adRevenue, autoCollected);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void reportAdRevenueIfNotAutoCollected() {
        AdRevenue adRevenue = mock(AdRevenue.class);
        boolean autoCollected = false;
        mReporterExtendedProxy.reportAdRevenue(adRevenue, autoCollected);

        InOrder inOrder = Mockito.inOrder(reporterBarrier, mSynchronousStageExecutor, mReporter);
        inOrder.verify(reporterBarrier).reportAdRevenue(adRevenue, autoCollected);
        inOrder.verify(mSynchronousStageExecutor).reportAdRevenue(adRevenue, autoCollected);
        inOrder.verify(mReporter).reportAdRevenue(adRevenue, autoCollected);
        inOrder.verifyNoMoreInteractions();
    }

    private ReporterExtendedProxy createProxyWithMockedExecutor() {
        return new ReporterExtendedProxy(
            contextRule.getContext(),
            reporterBarrier,
            mProvider,
            mSynchronousStageExecutor,
            ReporterConfig.newConfigBuilder(mApiKey).build(),
            pluginReporterProxy
        );
    }
}
