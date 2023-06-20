package io.appmetrica.analytics.impl.startup.executor;

import io.appmetrica.analytics.impl.startup.StartupUnit;
import io.appmetrica.analytics.networktasks.internal.NetworkCore;
import io.appmetrica.analytics.networktasks.internal.NetworkServiceLocator;
import io.appmetrica.analytics.networktasks.internal.NetworkTask;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class RegularStartupExecutorTest extends CommonTest {

    @Rule
    public final MockedStaticRule<NetworkServiceLocator> mRule =
            new MockedStaticRule<>(NetworkServiceLocator.class);

    @Mock
    private StartupUnit startupUnit;
    @Mock
    private NetworkServiceLocator networkServiceLocator;
    @Mock
    private NetworkCore networkCore;
    @Mock
    private NetworkTask networkTask;

    RegularStartupExecutor mExecutor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(NetworkServiceLocator.getInstance()).thenReturn(networkServiceLocator);
        when(networkServiceLocator.getNetworkCore()).thenReturn(networkCore);
        doReturn(networkTask).when(startupUnit).getOrCreateStartupTaskIfRequired();
        mExecutor = new RegularStartupExecutor(startupUnit);
    }

    @Test
    public void testNoStartupIfNoTasks() {
        doReturn(null).when(startupUnit).getOrCreateStartupTaskIfRequired();
        mExecutor.sendStartupIfRequired();
        verifyNoInteractions(networkCore);
    }

    @Test
    public void testExecuteTask() {
        mExecutor.sendStartupIfRequired();
        verify(networkCore).startTask(networkTask);
    }

}
