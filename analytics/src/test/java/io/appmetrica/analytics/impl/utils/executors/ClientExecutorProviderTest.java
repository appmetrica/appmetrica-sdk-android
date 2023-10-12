package io.appmetrica.analytics.impl.utils.executors;

import android.os.Handler;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ClientExecutorProviderTest extends CommonTest {

    @Mock
    private ClientExecutorFactory mClientExecutorFactory;
    @Mock
    private ExecutorWrapper mApiProxyExecutor;
    @Mock
    private ExecutorWrapper mReportsSenderExecutor;
    @Mock
    private ExecutorWrapper mDefaultExecutor;
    @Mock
    private Handler mHandler;

    private ClientExecutorProvider mClientExecutorProvider;

    private static final int CALLS_COUNT = 10;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(mClientExecutorFactory.createDefaultExecutor()).thenReturn(mDefaultExecutor);
        when(mClientExecutorFactory.createApiProxyExecutor()).thenReturn(mApiProxyExecutor);
        when(mClientExecutorFactory.createReportsSenderExecutor()).thenReturn(mReportsSenderExecutor);
        when(mClientExecutorFactory.createMainHandler()).thenReturn(mHandler);

        mClientExecutorProvider = new ClientExecutorProvider(mClientExecutorFactory);
    }

    @Test
    public void testDefaultConstructor() {
        mClientExecutorProvider = new ClientExecutorProvider();
        assertThat(mClientExecutorProvider.getThreadFactory()).isNotNull();
    }

    @Test
    public void testGetReportsSenderExecutor() {
        for (int i = 0; i < CALLS_COUNT; i++) {
            assertThat(mClientExecutorProvider.getReportSenderExecutor())
                    .as("Attempt #%d", i)
                    .isEqualTo(mReportsSenderExecutor);
        }
        verify(mClientExecutorFactory).createReportsSenderExecutor();
        verifyNoMoreInteractions(mClientExecutorFactory);
    }

    @Test
    public void testGetMainHandler() {
        for (int i = 0; i < CALLS_COUNT; i++) {
            assertThat(mClientExecutorProvider.getMainHandler())
                    .as("Attempt #%d", i)
                    .isEqualTo(mHandler);
        }
        verify(mClientExecutorFactory).createMainHandler();
        verifyNoMoreInteractions(mClientExecutorFactory);
    }

    @Test
    public void testGetApiProxyExecutor() {
        for (int i = 0; i < CALLS_COUNT; i++) {
            assertThat(mClientExecutorProvider.getApiProxyExecutor()).as("Attempt #%d", i).isEqualTo(mApiProxyExecutor);
        }
        verify(mClientExecutorFactory).createApiProxyExecutor();
        verifyNoMoreInteractions(mClientExecutorFactory);
    }

    @Test
    public void testGetDefaultWorkingExecutor() {
        for (int i = 0; i < CALLS_COUNT; i++) {
            assertThat(mClientExecutorProvider.getDefaultExecutor()).as("Attempt #%d", i).isEqualTo(mDefaultExecutor);
        }
        verify(mClientExecutorFactory).createDefaultExecutor();
        verifyNoMoreInteractions(mClientExecutorFactory);
    }
}
