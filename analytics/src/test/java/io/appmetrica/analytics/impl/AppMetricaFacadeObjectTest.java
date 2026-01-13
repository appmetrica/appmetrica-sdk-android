package io.appmetrica.analytics.impl;

import android.content.Context;
import io.appmetrica.analytics.AdvIdentifiersResult;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.AppMetricaLibraryAdapterConfig;
import io.appmetrica.analytics.DeferredDeeplinkListener;
import io.appmetrica.analytics.DeferredDeeplinkParametersListener;
import io.appmetrica.analytics.ReporterConfig;
import io.appmetrica.analytics.StartupParamsCallback;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.startup.Constants;
import io.appmetrica.analytics.impl.startup.uuid.MultiProcessSafeUuidProvider;
import io.appmetrica.analytics.impl.utils.FirstLaunchDetector;
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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
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
    private Thread initCoreThread;
    @Mock
    private ClientExecutorProvider clientExecutorProvider;
    @Mock
    private AppMetricaLibraryAdapterConfig adapterConfig;
    @Captor
    private ArgumentCaptor<Runnable> runnableArgumentCaptor;
    private Context mContext;
    private AppMetricaFacade mFacade;

    @Rule
    public final ClientServiceLocatorRule mClientServiceLocatorRule = new ClientServiceLocatorRule();

    @Rule
    public final MockedConstructionRule<ClientMigrationManager> clientMigrationManagerMockedConstructionRule =
        new MockedConstructionRule<>(ClientMigrationManager.class);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(ClientServiceLocator.getInstance().getAppMetricaCoreComponentsProvider().getCore(
            mContext, clientExecutorProvider
        )).thenReturn(mCore);
        when(ClientServiceLocator.getInstance().getAppMetricaCoreComponentsProvider().getImpl(
            mContext, mCore
        )).thenReturn(mImpl);
        when(ClientServiceLocator.getInstance().getClientExecutorProvider()).thenReturn(clientExecutorProvider);
        when(clientExecutorProvider.getDefaultExecutor()).thenReturn(executor);
        when(clientExecutorProvider.getCoreInitThread(any())).thenReturn(initCoreThread);
        AppMetricaFacade.killInstance();
        mFacade = new AppMetricaFacade(mContext);
    }

    @Test
    public void futureIsInited() {
        mFacade.init();
        touchInitThread();
        assertThat(mFacade.isFullInitFutureDone()).isTrue();
    }

    @Test
    public void futureIsInitedAsynchronously() {
        mFacade.init();
        assertThat(mFacade.isFullInitFutureDone()).isFalse();
        touchInitThread();
        assertThat(mFacade.isFullInitFutureDone()).isTrue();
    }

    @Test
    public void futureIsNotInited() {
        mFacade.init();
        assertThat(mFacade.isFullInitFutureDone()).isFalse();
        touchInitThread();

        FirstLaunchDetector firstLaunchDetector = ClientServiceLocator.getInstance().getFirstLaunchDetector();
        MultiProcessSafeUuidProvider multiProcessSafeUuidProvider =
            ClientServiceLocator.getInstance().getMultiProcessSafeUuidProvider(mContext);
        InOrder inOrder = inOrder(firstLaunchDetector, multiProcessSafeUuidProvider);
        inOrder.verify(firstLaunchDetector).init(mContext);
        inOrder.verify(multiProcessSafeUuidProvider).readUuid();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void activateCore() {
        mFacade.init();
        AppMetricaConfig config = mock(AppMetricaConfig.class);
        mFacade.activateCore(config);
        verify(mCore).activate(config, mFacade);
    }

    @Test
    public void activateFull() {
        mFacade.init();
        touchInitThread();
        AppMetricaConfig config = mock(AppMetricaConfig.class);
        mFacade.activateFull(config);
        verify(mImpl).activate(config);
    }

    @Test
    public void activateFullWithoutConfig() {
        mFacade.init();
        touchInitThread();
        mFacade.activateFull(adapterConfig);
        verify(mImpl).activateAnonymously(adapterConfig);
    }

    @Test
    public void getMainReporterApiConsumerProvider() {
        mFacade.init();
        touchInitThread();
        MainReporterApiConsumerProvider mainReporterApiConsumerProvider = mock(MainReporterApiConsumerProvider.class);
        when(mImpl.getMainReporterApiConsumerProvider()).thenReturn(mainReporterApiConsumerProvider);
        assertThat(mFacade.getMainReporterApiConsumerProvider()).isSameAs(mainReporterApiConsumerProvider);
    }

    @Test
    public void requestDeferredDeeplinkParameters() {
        mFacade.init();
        touchInitThread();
        DeferredDeeplinkParametersListener listener = mock(DeferredDeeplinkParametersListener.class);
        mFacade.requestDeferredDeeplinkParameters(listener);
        verify(mImpl).requestDeferredDeeplinkParameters(listener);
    }

    @Test
    public void requestDeferredDeeplink() {
        mFacade.init();
        touchInitThread();
        DeferredDeeplinkListener listener = mock(DeferredDeeplinkListener.class);
        mFacade.requestDeferredDeeplink(listener);
        verify(mImpl).requestDeferredDeeplink(listener);
    }

    @Test
    public void activateReporter() {
        mFacade.init();
        touchInitThread();
        ReporterConfig config = mock(ReporterConfig.class);
        mFacade.activateReporter(config);
        verify(mImpl).activateReporter(config);
    }

    @Test
    public void getReporter() {
        mFacade.init();
        touchInitThread();
        ReporterConfig config = mock(ReporterConfig.class);
        IReporterExtended reporter = mock(IReporterExtended.class);
        when(mImpl.getReporter(config)).thenReturn(reporter);
        assertThat(mFacade.getReporter(config)).isSameAs(reporter);
    }

    @Test
    public void getDeviceId() {
        mFacade.init();
        touchInitThread();
        String deviceId = "3467583476";
        when(mImpl.getDeviceId()).thenReturn(deviceId);
        assertThat(mFacade.getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    public void getClids() {
        mFacade.init();
        touchInitThread();
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("key1", "value1");
        clids.put("key2", "value2");
        when(mImpl.getClids()).thenReturn(clids);
        assertThat(mFacade.getClids()).isEqualTo(clids);
    }

    @Test
    public void getCachedAdvIdentifiers() {
        mFacade.init();
        touchInitThread();
        AdvIdentifiersResult result = mock(AdvIdentifiersResult.class);
        when(mImpl.getCachedAdvIdentifiers()).thenReturn(result);
        assertThat(mFacade.getCachedAdvIdentifiers()).isSameAs(result);
    }

    @Test
    public void requestStartupParamsWithStartupParamsCallback() {
        mFacade.init();
        touchInitThread();
        StartupParamsCallback callback = mock(StartupParamsCallback.class);
        List<String> params = Arrays.asList(Constants.StartupParamsCallbackKeys.DEVICE_ID, Constants.StartupParamsCallbackKeys.UUID);
        mFacade.requestStartupParams(callback, params);
        verify(mImpl).requestStartupParams(callback, params);
    }

    @Test
    public void getClientTimeTracker() {
        mFacade.init();
        ClientTimeTracker clientTimeTracker = mock(ClientTimeTracker.class);
        when(mCore.getClientTimeTracker()).thenReturn(clientTimeTracker);
        assertThat(mFacade.getClientTimeTracker()).isSameAs(clientTimeTracker);
    }

    @Test
    public void getFeatures() {
        mFacade.init();
        touchInitThread();
        FeaturesResult features = mock(FeaturesResult.class);
        when(mImpl.getFeatures()).thenReturn(features);
        assertThat(mFacade.getFeatures()).isSameAs(features);
    }

    private void touchInitThread() {
        verify(ClientServiceLocator.getInstance().getClientExecutorProvider())
            .getCoreInitThread(runnableArgumentCaptor.capture());
        verify(initCoreThread).start();
        runnableArgumentCaptor.getValue().run();
    }
}
