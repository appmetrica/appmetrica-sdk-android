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
    private AppMetricaCoreComponentsProvider coreComponentsProvider;
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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mContext = RuntimeEnvironment.getApplication();
        when(clientExecutorProvider.getDefaultExecutor()).thenReturn(executor);
        when(coreComponentsProvider.getImpl(mContext, mCore)).thenReturn(mImpl);
        AppMetricaFacade.killInstance();
        mFacade = new AppMetricaFacade(
                mContext, coreComponentsProvider, mCore, ClientServiceLocator.getInstance().getClientExecutorProvider()
        );
    }

    @Test(expected = RuntimeException.class)
    public void exceptionWhileCreatingImpl() throws Exception {
        when(coreComponentsProvider.getImpl(mContext, mCore)).thenThrow(new NullPointerException());
        AppMetricaFacade.killInstance();
        mFacade = new AppMetricaFacade(
                mContext, coreComponentsProvider, mCore, mClientServiceLocatorRule.clientExecutorProvider
        );
        mFacade.getClids();
    }

    @Test
    public void futureIsInited() {
        assertThat(mFacade.isFullyInitialized()).isTrue();
    }

    @Test
    public void futureIsNotInited() {
        mFacade = new AppMetricaFacade(mContext, coreComponentsProvider, mCore, clientExecutorProvider);
        // One for warm up uuid and one more for full init future
        verify(executor, times(2)).execute(runnableArgumentCaptor.capture());
        assertThat(mFacade.isFullyInitialized()).isFalse();
        runnableArgumentCaptor.getAllValues().get(0).run();

        //One from setUp and one more - at this test
        verify(clientMigrationManagerMockedConstructionRule.getConstructionMock().constructed().get(1))
            .checkMigration(mContext);
        assertThat(clientMigrationManagerMockedConstructionRule.getConstructionMock().constructed()).hasSize(2);
        assertThat(clientMigrationManagerMockedConstructionRule.getArgumentInterceptor().flatArguments())
            .containsOnly(mContext);
        verify(ClientServiceLocator.getInstance().getMultiProcessSafeUuidProvider(mContext), times(2)).readUuid();
    }

    @Test
    public void activateCore() {
        AppMetricaConfig config = mock(AppMetricaConfig.class);
        mFacade.activateCore(config);
        verify(mCore).activate(config, mFacade);
    }

    @Test
    public void activateFull() {
        AppMetricaConfig originalConfig = mock(AppMetricaConfig.class);
        AppMetricaConfig config = mock(AppMetricaConfig.class);
        mFacade.activateFull(originalConfig, config);
        verify(mImpl).activate(originalConfig, config);
    }

    @Test
    public void getMainReporterApiConsumerProvider() {
        MainReporterApiConsumerProvider mainReporterApiConsumerProvider = mock(MainReporterApiConsumerProvider.class);
        when(mImpl.getMainReporterApiConsumerProvider()).thenReturn(mainReporterApiConsumerProvider);
        assertThat(mFacade.getMainReporterApiConsumerProvider()).isSameAs(mainReporterApiConsumerProvider);
    }

    @Test
    public void requestDeferredDeeplinkParameters() {
        DeferredDeeplinkParametersListener listener = mock(DeferredDeeplinkParametersListener.class);
        mFacade.requestDeferredDeeplinkParameters(listener);
        verify(mImpl).requestDeferredDeeplinkParameters(listener);
    }

    @Test
    public void requestDeferredDeeplink() {
        DeferredDeeplinkListener listener = mock(DeferredDeeplinkListener.class);
        mFacade.requestDeferredDeeplink(listener);
        verify(mImpl).requestDeferredDeeplink(listener);
    }

    @Test
    public void activateReporter() {
        ReporterConfig config = mock(ReporterConfig.class);
        mFacade.activateReporter(config);
        verify(mImpl).activateReporter(config);
    }

    @Test
    public void getReporter() {
        ReporterConfig config = mock(ReporterConfig.class);
        IReporterExtended reporter = mock(IReporterExtended.class);
        when(mImpl.getReporter(config)).thenReturn(reporter);
        assertThat(mFacade.getReporter(config)).isSameAs(reporter);
    }

    @Test
    public void getDeviceId() {
        String deviceId = "3467583476";
        when(mImpl.getDeviceId()).thenReturn(deviceId);
        assertThat(mFacade.getDeviceId()).isEqualTo(deviceId);
    }

    @Test
    public void getClids() {
        Map<String, String> clids = new HashMap<String, String>();
        clids.put("key1", "value1");
        clids.put("key2", "value2");
        when(mImpl.getClids()).thenReturn(clids);
        assertThat(mFacade.getClids()).isEqualTo(clids);
    }

    @Test
    public void getCachedAdvIdentifiers() {
        AdvIdentifiersResult result = mock(AdvIdentifiersResult.class);
        when(mImpl.getCachedAdvIdentifiers()).thenReturn(result);
        assertThat(mFacade.getCachedAdvIdentifiers()).isSameAs(result);
    }

    @Test
    public void requestStartupParamsWithStartupParamsCallback() {
        StartupParamsCallback callback = mock(StartupParamsCallback.class);
        List<String> params = Arrays.asList(Constants.StartupParamsCallbackKeys.DEVICE_ID, Constants.StartupParamsCallbackKeys.UUID);
        mFacade.requestStartupParams(callback, params);
        verify(mImpl).requestStartupParams(callback, params);
    }

    @Test
    public void getClientTimeTracker() {
        ClientTimeTracker clientTimeTracker = mock(ClientTimeTracker.class);
        when(mCore.getClientTimeTracker()).thenReturn(clientTimeTracker);
        assertThat(mFacade.getClientTimeTracker()).isSameAs(clientTimeTracker);
    }

    @Test
    public void getFeatures() {
        FeaturesResult features = mock(FeaturesResult.class);
        when(mImpl.getFeatures()).thenReturn(features);
        assertThat(mFacade.getFeatures()).isSameAs(features);
    }
}
