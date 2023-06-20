package io.appmetrica.analytics.impl.utils.executors;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ServiceExecutorFactoryTest extends CommonTest {

    @Mock
    private Runnable runnable;

    private ServiceExecutorFactory serviceExecutorFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        serviceExecutorFactory = new ServiceExecutorFactory();
    }

    @Test
    public void testCreateMetricaCoreExecutor() {
        assertThat(serviceExecutorFactory.createMetricaCoreExecutor().getLooper().getThread().getName())
            .startsWith("IAA-SC");
    }

    @Test
    public void testCreateSynchronizedBlockingExecutor() {
        assertThat(serviceExecutorFactory.createSynchronizedBlockingExecutor()).isNotNull();
    }

    @Test
    public void testCreateReportRunnableExecutor() {
        assertThat(serviceExecutorFactory.createReportRunnableExecutor().getLooper().getThread().getName())
            .startsWith("IAA-STE");
    }

    @Test
    public void testCreateModuleExecutor() {
        assertThat(serviceExecutorFactory.createModuleExecutor().getLooper().getThread().getName())
            .startsWith("IAA-SMH-1");
    }

    @Test
    public void testCreateNetworkTaskProcessorExecutor() {
        assertThat(serviceExecutorFactory.createNetworkTaskProcessorExecutor().getLooper().getThread().getName())
            .startsWith("IAA-SNTPE");
    }

    @Test
    public void testCreateSupportDataCollectingExecutor() {
        assertThat(serviceExecutorFactory.createSupportIOExecutor().getLooper().getThread().getName())
            .startsWith("IAA-SIO");
    }

    @Test
    public void testCreateDefaultExecutor() {
        assertThat(serviceExecutorFactory.createDefaultExecutor().getLooper().getThread().getName())
            .startsWith("IAA-SDE");
    }

    @Test
    public void createCustomExecutor() {
        String tag = "some_tag";
        assertThat(serviceExecutorFactory.createCustomModuleExecutor(tag).getLooper().getThread().getName())
            .startsWith("IAA-M-" + tag);
    }

    @Test
    public void createHmsReferrerThread() {
        assertThat(serviceExecutorFactory.createHmsReferrerThread(runnable).getName()).startsWith("IAA-SHMSR");
    }
}
