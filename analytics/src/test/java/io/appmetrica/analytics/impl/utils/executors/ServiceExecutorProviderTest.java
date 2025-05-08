package io.appmetrica.analytics.impl.utils.executors;

import io.appmetrica.analytics.coreapi.internal.executors.InterruptionSafeThread;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ServiceExecutorProviderTest extends CommonTest {

    @Mock
    private ServiceExecutorFactory serviceExecutorFactory;
    @Mock
    private ExecutorWrapper coreExecutor;
    @Mock
    private Executor synchronizedBlockingExecutor;
    @Mock
    private ExecutorWrapper reportRunnableExecutor;
    @Mock
    private ExecutorWrapper moduleExecutor;
    @Mock
    private ExecutorWrapper networkTaskProcessorExecutor;
    @Mock
    private ExecutorWrapper supportDataCollectingExecutor;
    @Mock
    private ExecutorWrapper defaultExecutor;
    @Mock
    private Runnable runnable;
    @Mock
    private InterruptionSafeThread hmsReferrerThread;
    @Mock
    private ExecutorWrapper firstCustomModuleExecutor;
    @Mock
    private ExecutorWrapper secondCustomModuleExecutor;

    private final String firstCustomExecutorTag = "first";
    private final String secondCustomModuleExecutorTag = "second";

    private ServiceExecutorProvider serviceExecutorProvider;

    private static final int CALLS_COUNT = 10;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        serviceExecutorProvider = new ServiceExecutorProvider(serviceExecutorFactory);
        when(serviceExecutorFactory.createMetricaCoreExecutor()).thenReturn(coreExecutor);
        when(serviceExecutorFactory.createSynchronizedBlockingExecutor()).thenReturn(synchronizedBlockingExecutor);
        when(serviceExecutorFactory.createReportRunnableExecutor()).thenReturn(reportRunnableExecutor);
        when(serviceExecutorFactory.createModuleExecutor()).thenReturn(moduleExecutor);
        when(serviceExecutorFactory.createNetworkTaskProcessorExecutor()).thenReturn(networkTaskProcessorExecutor);
        when(serviceExecutorFactory.createSupportIOExecutor()).thenReturn(supportDataCollectingExecutor);
        when(serviceExecutorFactory.createDefaultExecutor()).thenReturn(defaultExecutor);
        when(serviceExecutorFactory.createHmsReferrerThread(runnable)).thenReturn(hmsReferrerThread);
        when(serviceExecutorFactory.createCustomModuleExecutor(firstCustomExecutorTag))
            .thenReturn(firstCustomModuleExecutor);
        when(serviceExecutorFactory.createCustomModuleExecutor(secondCustomModuleExecutorTag))
            .thenReturn(secondCustomModuleExecutor);
    }

    @Test
    public void testDefaultConstructor() {
        serviceExecutorProvider = new ServiceExecutorProvider();
        assertThat(serviceExecutorProvider.getServiceExecutorFactory()).isNotNull();
    }

    @Test
    public void testGetMetricaCoreExecutor() {
        for (int i = 0; i < CALLS_COUNT; i++) {
            assertThat(serviceExecutorProvider.getMetricaCoreExecutor())
                .as("Attempt #%d", i)
                .isEqualTo(coreExecutor);
        }
        verify(serviceExecutorFactory).createMetricaCoreExecutor();
        verifyNoMoreInteractions(serviceExecutorFactory);
    }

    @Test
    public void testGetReportRunnableExecutor() {
        for (int i = 0; i < CALLS_COUNT; i++) {
            assertThat(serviceExecutorProvider.getReportRunnableExecutor())
                .as("Attempt #%d", i)
                .isEqualTo(reportRunnableExecutor);
        }
        verify(serviceExecutorFactory).createReportRunnableExecutor();
        verifyNoMoreInteractions(serviceExecutorFactory);
    }

    @Test
    public void testGetModuleExecutor() {
        for (int i = 0; i < CALLS_COUNT; i++) {
            assertThat(serviceExecutorProvider.getModuleExecutor())
                .as("Attempt #%d", i)
                .isEqualTo(moduleExecutor);
        }
        verify(serviceExecutorFactory).createModuleExecutor();
        verifyNoMoreInteractions(serviceExecutorFactory);
    }

    @Test
    public void testGetNetworkTaskProcessorExecutor() {
        for (int i = 0; i < CALLS_COUNT; i++) {
            assertThat(serviceExecutorProvider.getNetworkTaskProcessorExecutor())
                .as("Attempt #%d", i)
                .isEqualTo(networkTaskProcessorExecutor);
        }
        verify(serviceExecutorFactory).createNetworkTaskProcessorExecutor();
        verifyNoMoreInteractions(serviceExecutorFactory);
    }

    @Test
    public void testSupportDataCollectingExecutor() {
        for (int i = 0; i < CALLS_COUNT; i++) {
            assertThat(serviceExecutorProvider.getSupportIOExecutor())
                .as("Attempt #%d", i)
                .isEqualTo(supportDataCollectingExecutor);
        }
        verify(serviceExecutorFactory).createSupportIOExecutor();
        verifyNoMoreInteractions(serviceExecutorFactory);
    }

    @Test
    public void testGetDefaultExecutor() {
        for (int i = 0; i < CALLS_COUNT; i++) {
            assertThat(serviceExecutorProvider.getDefaultExecutor())
                .as("Attempt #%d", i)
                .isEqualTo(defaultExecutor);
        }
        verify(serviceExecutorFactory).createDefaultExecutor();
        verifyNoMoreInteractions(serviceExecutorFactory);
    }

    @Test
    public void getCustomModuleExecutor() {
        for (int i = 0; i < CALLS_COUNT; i++) {
            assertThat(serviceExecutorProvider.getCustomModuleExecutor(firstCustomExecutorTag))
                .isEqualTo(firstCustomModuleExecutor);
            assertThat(serviceExecutorProvider.getCustomModuleExecutor(secondCustomModuleExecutorTag))
                .isEqualTo(secondCustomModuleExecutor);
        }
        verify(serviceExecutorFactory).createCustomModuleExecutor(firstCustomExecutorTag);
        verify(serviceExecutorFactory).createCustomModuleExecutor(secondCustomModuleExecutorTag);
        verifyNoMoreInteractions(serviceExecutorFactory);
    }

    @Test
    public void getHmsReferrerThread() {
        for (int i = 0; i < CALLS_COUNT; i++) {
            assertThat(serviceExecutorProvider.getHmsReferrerThread(runnable)).isEqualTo(hmsReferrerThread);
        }
        verify(serviceExecutorFactory, times(CALLS_COUNT)).createHmsReferrerThread(runnable);
    }
}
