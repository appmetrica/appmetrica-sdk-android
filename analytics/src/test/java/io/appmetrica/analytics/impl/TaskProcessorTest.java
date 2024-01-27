package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.impl.component.ComponentUnit;
import io.appmetrica.analytics.impl.startup.executor.StartupExecutor;
import io.appmetrica.analytics.networktasks.internal.NetworkTask;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class TaskProcessorTest extends CommonTest {

    private TaskProcessor processor;

    @Mock
    private ComponentUnit componentUnit;
    @Mock
    private StartupExecutor startupExecutor;
    @Mock
    private NetworkTask networkTask;

    @Rule
    public GlobalServiceLocatorRule globalServiceLocatorRule = new GlobalServiceLocatorRule();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        processor = new TaskProcessor(componentUnit, startupExecutor);
    }

    @Test
    public void testStartupValidityCheckedOnFlush() {
        processor.flushAllTasks();
        verify(startupExecutor).sendStartupIfRequired();
    }

    @Test
    public void flushAfterOnDestroyAndOnCreate() {
        processor.onCreate();
        processor.onDestroy();
        processor.flushAllTasks();
        verifyNoMoreInteractions(startupExecutor);
        processor.onCreate();
        processor.flushAllTasks();
        verify(startupExecutor).sendStartupIfRequired();
    }

    @Test
    public void onCreateTwice() {
        processor.onCreate();
        processor.onCreate();
        processor.flushAllTasks();
        verify(startupExecutor).sendStartupIfRequired();
    }

    @Test
    public void onDestroyTwice() {
        processor.onCreate();
        processor.onDestroy();
        processor.onDestroy();
        processor.flushAllTasks();
        verifyNoMoreInteractions(startupExecutor);
    }

    @Test
    public void onDestroyWithoutOnCreate() {
        processor.onDestroy();
        processor.flushAllTasks();
        verifyNoMoreInteractions(startupExecutor);
    }

    @Test
    public void startTask() {
        processor.startTask(networkTask);
        verify(GlobalServiceLocator.getInstance().getNetworkCore()).startTask(networkTask);
    }

    @Test
    public void getComponent() {
        assertThat(processor.getComponent()).isEqualTo(componentUnit);
    }
}
