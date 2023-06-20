package io.appmetrica.analytics.impl.stub;

import android.os.Handler;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.ClientTimeTracker;
import io.appmetrica.analytics.impl.IReporterFactoryProvider;
import io.appmetrica.analytics.impl.id.AdvertisingIdGetterStub;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppAppMetricaServiceCoreStubTest extends CommonTest {

    @Mock
    private Handler handler;
    @Mock
    private IHandlerExecutor handlerExecutor;
    @Mock
    private ICommonExecutor apiProxyExecutor;
    @Mock
    private ClientExecutorProvider clientExecutorProvider;
    @Mock
    private ClientTimeTracker clientTimeTracker;
    @Mock
    private AppMetricaConfig appMetricaConfig;
    @Mock
    private IReporterFactoryProvider reporterFactoryProvider;
    @Mock
    private Throwable throwable;

    private AppMetricaCoreStub stub;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(clientExecutorProvider.getDefaultExecutor()).thenReturn(handlerExecutor);
        when(clientExecutorProvider.getApiProxyExecutor()).thenReturn(apiProxyExecutor);
        when(handlerExecutor.getHandler()).thenReturn(handler);

        stub = new AppMetricaCoreStub(handlerExecutor, handler, apiProxyExecutor, clientTimeTracker);
    }

    @Test
    public void constructor() throws Exception {
        ObjectPropertyAssertions(new AppMetricaCoreStub(clientExecutorProvider))
                .withPrivateFields(true)
                .checkField("executor", handlerExecutor)
                .checkField("metricaHandler", handler)
                .checkField("apiProxyExecutor", apiProxyExecutor)
                .checkFieldsNonNull("clientTimeTracker")
                .checkAll();
    }

    @Test
    public void activate() {
        stub.activate(appMetricaConfig, reporterFactoryProvider);
        verifyZeroInteractions(appMetricaConfig, reporterFactoryProvider);
    }

    @Test
    public void getMetricaHandler() {
        assertThat(stub.getMetricaHandler()).isEqualTo(handler);
    }

    @Test
    public void getClientTimeTracker() {
        assertThat(stub.getClientTimeTracker()).isEqualTo(clientTimeTracker);
    }

    @Test
    public void getAdvertisingIdGetter() {
        assertThat(stub.getAdvertisingIdGetter())
                .isNotNull()
                .isInstanceOf(AdvertisingIdGetterStub.class);
    }

    @Test
    public void getExecutor() {
        assertThat(stub.getExecutor()).isEqualTo(handlerExecutor);
    }

    @Test
    public void getAppOpenWatcher() {
        assertThat(stub.getAppOpenWatcher()).isNotNull();
    }
}
