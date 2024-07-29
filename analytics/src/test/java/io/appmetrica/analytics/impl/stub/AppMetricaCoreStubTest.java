package io.appmetrica.analytics.impl.stub;

import android.os.Handler;
import io.appmetrica.analytics.AppMetricaConfig;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.impl.ClientServiceLocator;
import io.appmetrica.analytics.impl.ClientTimeTracker;
import io.appmetrica.analytics.impl.IReporterFactoryProvider;
import io.appmetrica.analytics.impl.utils.executors.ClientExecutorProvider;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaCoreStubTest extends CommonTest {

    @Mock
    private Handler handler;
    @Mock
    private IHandlerExecutor handlerExecutor;
    @Mock
    private ClientExecutorProvider clientExecutorProvider;
    @Mock
    private AppMetricaConfig appMetricaConfig;
    @Mock
    private IReporterFactoryProvider reporterFactoryProvider;

    @Rule
    public ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

    @Rule
    public MockedConstructionRule<ClientTimeTracker> clientTimeTrackerMockedConstructionRule =
        new MockedConstructionRule<>(ClientTimeTracker.class);

    private AppMetricaCoreStub stub;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(ClientServiceLocator.getInstance().getClientExecutorProvider()).thenReturn(clientExecutorProvider);
        when(clientExecutorProvider.getDefaultExecutor()).thenReturn(handlerExecutor);
        when(handlerExecutor.getHandler()).thenReturn(handler);

        stub = new AppMetricaCoreStub();
    }

    @Test
    public void activate() {
        stub.activate(appMetricaConfig, reporterFactoryProvider);
        verifyNoMoreInteractions(appMetricaConfig, reporterFactoryProvider);
    }

    @Test
    public void getMetricaHandler() {
        assertThat(stub.getDefaultHandler()).isEqualTo(handler);
    }

    @Test
    public void getClientTimeTracker() {
        assertThat(stub.getClientTimeTracker())
            .isEqualTo(clientTimeTrackerMockedConstructionRule.getConstructionMock().constructed().get(0));
        assertThat(clientTimeTrackerMockedConstructionRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(clientTimeTrackerMockedConstructionRule.getArgumentInterceptor().flatArguments()).isEmpty();
    }

    @Test
    public void getExecutor() {
        assertThat(stub.getDefaultExecutor()).isEqualTo(handlerExecutor);
    }

    @Test
    public void getAppOpenWatcher() {
        assertThat(stub.getAppOpenWatcher()).isNotNull();
    }

    @Test
    public void getJvmCrashClientController() {
        assertThat(stub.getJvmCrashClientController()).isNotNull();
    }
}
