package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.startup.Constants;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaFacadeObjectTest extends CommonTest {

    @Mock
    private AppMetricaCore mCore;
    @Mock
    private AppMetricaImpl mImpl;
    @Mock
    private IHandlerExecutor executor;
    @Mock
    private ClientExecutorProvider clientExecutorProvider;
    @Captor
    private ArgumentCaptor<Runnable> runnableArgumentCaptor;
    private Context mContext;
    private AppMetricaFacade mFacade;

    @Rule
    public final ClientServiceLocatorRule mClientServiceLocatorRule = new ClientServiceLocatorRule();

    @Rule
    public final MockedConstructionRule<ClientMigrationManager> clientMigrationManagerMockedConstructionRule =
        new MockedConstructionRule<>(ClientMigrationManager.class);

    @Rule
    public final MockedConstructionRule<AppMetricaCoreComponentsProvider> coreComponentsProviderConstructionRule =
        new MockedConstructionRule<>(
            AppMetricaCoreComponentsProvider.class,
            new MockedConstruction.MockInitializer<AppMetricaCoreComponentsProvider>() {
                @Override
                public void prepare(AppMetricaCoreComponentsProvider mock,
                                    MockedConstruction.Context context) throws Throwable {
                    when(mock.getImpl(mContext, mCore)).thenReturn(mImpl);
                    when(mock.getCore(mContext, clientExecutorProvider)).thenReturn(mCore);
                }
            }
        );
    private AppMetricaCoreComponentsProvider coreComponentsProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(ClientServiceLocator.getInstance().getClientExecutorProvider()).thenReturn(clientExecutorProvider);
        when(clientExecutorProvider.getDefaultExecutor()).thenReturn(executor);
        AppMetricaFacade.killInstance();
        mFacade = new AppMetricaFacade(mContext);
        coreComponentsProvider = coreComponentsProviderConstructionRule.getConstructionMock().constructed().get(0);
    }

    @Test
    public void futureIsInited() {
        mFacade.init(false);
        assertThat(mFacade.isFullyInitialized()).isTrue();
    }

    @Test
    public void futureIsInitedAsynchronously() {
        mFacade.init(true);
        assertThat(mFacade.isFullyInitialized()).isFalse();
        verify(executor, times(2)).execute(runnableArgumentCaptor.capture());
        for (Runnable runnable : runnableArgumentCaptor.getAllValues()) {
            runnable.run();
        }
        assertThat(mFacade.isFullyInitialized()).isTrue();
    }

    @Test
    public void futureIsNotInited() {
        mFacade.init(true);
        // One for warm up uuid and one more for full init future
        verify(executor, times(2)).execute(runnableArgumentCaptor.capture());
        assertThat(mFacade.isFullyInitialized()).isFalse();
        runnableArgumentCaptor.getAllValues().get(0).run();

        //One from setUp and one more - at this test
        verify(clientMigrationManagerMockedConstructionRule.getConstructionMock().constructed().get(0))
            .checkMigration(mContext);
        assertThat(clientMigrationManagerMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(clientMigrationManagerMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsOnly(mContext);
        verify(ClientServiceLocator.getInstance().getMultiProcessSafeUuidProvider(mContext), times(1)).readUuid();
    }

    @Test
    public void activateCore() {
        mFacade.init(false);
        AppMetricaConfig config = mock(AppMetricaConfig.class);
        mFacade.activateCore(config);
        verify(mCore).activate(config, mFacade);
    }

    @Test
    public void activateFull() {
        mFacade.init(false);
        AppMetricaConfig config = mock(AppMetricaConfig.class);
        mFacade.activateFull(config);
        verify(mImpl).activate(config);
    }

    @Test
    public void activateFullWithoutConfig() {
        mFacade.init(false);
        mFacade.activateFull();
        verify(mImpl).activateAnonymously();
    }

    @Test
    public void getMainReporterApiConsumerProvider() {
        mFacade.init(false);
        MainReporterApiConsumerProvider mainReporterApiConsumerProvider = mock(MainReporterApiConsumerProvider.class);
        when(mImpl.getMainReporterApiConsumerProvider()).thenReturn(mainReporterApiConsumerProvider);
        assertThat(mFacade.getMainReporterApiConsumerProvider()).isSameAs(mainReporterApiConsumerProvider);
    }

    @Test
    public void requestDeferredDeeplinkParameters() {
        mFacade.init(false);
        DeferredDeeplinkParametersListener listener = mock(DeferredDeeplinkParametersListener.class);
        mFacade.requestDeferredDeeplinkParameters(listener);
        verify(mImpl).requestDeferredDeeplinkParameters(listener);
    }

    @Test
    public void requestDeferredDeeplink() {
        mFacade.init(false);
        DeferredDeeplinkListener listener = mock(DeferredDeeplinkListener.class);
        mFacade.requestDeferredDeeplink(listener);
        verify(mImpl).requestDeferredDeeplink(listener);
    }

    @Test
    public void activateReporter() {
        mFacade.init(false);
        ReporterConfig config = mock(ReporterConfig.class);
        mFacade.activateReporter(config);
        verify(mImpl).activateReporter(config);
    }

    @Test
    public void getReporter() {
        mFacade.init(false);
        ReporterConfig config = mock(ReporterConfig.class);
        IReporterExtended reporter = mock(IReporterExtended.class);
        when(mImpl.getReporter(config)).thenReturn(reporter);
        assertThat(mFacade.getReporter(config)).isSameAs(reporter);
    }

    @Test
    public void getDeviceId() {
        mFacade.init(false);
        String deviceId = "3467583476";
        when(mImpl.getDeviceId()).thenReturn(deviceId);
        assertThat(mFacade.getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    public void getClids() {
        mFacade.init(false);
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("key1", "value1");
        clids.put("key2", "value2");
        when(mImpl.getClids()).thenReturn(clids);
        assertThat(mFacade.getClids()).isEqualTo(clids);
    }

    @Test
    public void getCachedAdvIdentifiers() {
        mFacade.init(false);
        AdvIdentifiersResult result = mock(AdvIdentifiersResult.class);
        when(mImpl.getCachedAdvIdentifiers()).thenReturn(result);
        assertThat(mFacade.getCachedAdvIdentifiers()).isSameAs(result);
    }

    @Test
    public void requestStartupParamsWithStartupParamsCallback() {
        mFacade.init(false);
        StartupParamsCallback callback = mock(StartupParamsCallback.class);
        List<String> params = Arrays.asList(Constants.StartupParamsCallbackKeys.DEVICE_ID, Constants.StartupParamsCallbackKeys.UUID);
        mFacade.requestStartupParams(callback, params);
        verify(mImpl).requestStartupParams(callback, params);
    }

    @Test
    public void getClientTimeTracker() {
        mFacade.init(false);
        ClientTimeTracker clientTimeTracker = mock(ClientTimeTracker.class);
        when(mCore.getClientTimeTracker()).thenReturn(clientTimeTracker);
        assertThat(mFacade.getClientTimeTracker()).isSameAs(clientTimeTracker);
    }

    @Test
    public void getFeatures() {
        mFacade.init(false);
        FeaturesResult features = mock(FeaturesResult.class);
        when(mImpl.getFeatures()).thenReturn(features);
        assertThat(mFacade.getFeatures()).isSameAs(features);
    }
}
