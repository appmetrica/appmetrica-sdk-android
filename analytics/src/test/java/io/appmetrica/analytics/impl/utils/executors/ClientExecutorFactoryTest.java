package io.appmetrica.analytics.impl.utils.executors;

import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ClientExecutorFactoryTest extends CommonTest {

    private ClientExecutorFactory mClientExecutorFactory;

    @Before
    public void setUp() throws Exception {
        mClientExecutorFactory = new ClientExecutorFactory();
    }

    @Test
    public void testCreateApiProxyExecutor() {
        assertThat(mClientExecutorFactory.createApiProxyExecutor().getLooper().getThread().getName())
                .startsWith("IAA-CAPT");
    }

    @Test
    public void testCreateReportsSenderExecutor() {
        assertThat(mClientExecutorFactory.createReportsSenderExecutor().getLooper().getThread().getName())
                .startsWith("IAA-CRS");
    }

    @Test
    public void testCreateDefaultExecutor() {
        assertThat(mClientExecutorFactory.createDefaultExecutor().getLooper().getThread().getName())
                .startsWith("IAA-CDE");
    }
}
