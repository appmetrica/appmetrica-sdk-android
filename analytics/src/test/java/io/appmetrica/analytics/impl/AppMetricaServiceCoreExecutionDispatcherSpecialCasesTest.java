package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.service.AppMetricaServiceCallback;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaServiceCoreExecutionDispatcherSpecialCasesTest extends CommonTest {

    @Mock
    private ICommonExecutor executor;
    @Mock
    private AppMetricaServiceCore appMetricaServiceCore;
    @Mock
    private LifecycleDependentComponentManager lifecycleDependentComponentManager;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    private AppMetricaServiceCoreExecutionDispatcher appMetricaCoreExecutionDispatcher;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        appMetricaCoreExecutionDispatcher = new AppMetricaServiceCoreExecutionDispatcher(
                executor, appMetricaServiceCore, lifecycleDependentComponentManager
        );
    }

    @Test
    public void onCreateNotDestroyed() {
        appMetricaCoreExecutionDispatcher.onCreate();
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        InOrder inOrder = Mockito.inOrder(lifecycleDependentComponentManager, appMetricaServiceCore);
        inOrder.verify(lifecycleDependentComponentManager).onCreate();
        inOrder.verify(appMetricaServiceCore).onCreate();
    }

    @Test
    public void onCreateDestroyed() {
        appMetricaCoreExecutionDispatcher.onDestroy();
        clearInvocations(lifecycleDependentComponentManager, appMetricaServiceCore);
        appMetricaCoreExecutionDispatcher.onCreate();
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(lifecycleDependentComponentManager).onCreate();
        verify(appMetricaServiceCore).onCreate();
    }

    @Test
    public void onCreateDestroyedWhenExecuting() {
        appMetricaCoreExecutionDispatcher.onCreate();
        verify(executor).execute(runnableCaptor.capture());
        appMetricaCoreExecutionDispatcher.onDestroy();
        runnableCaptor.getValue().run();
        verify(lifecycleDependentComponentManager, never()).onCreate();
        verify(appMetricaServiceCore, never()).onCreate();
    }

    @Test
    public void onDestroy() {
        appMetricaCoreExecutionDispatcher.onDestroy();
        InOrder inOrder = Mockito.inOrder(executor, lifecycleDependentComponentManager, appMetricaServiceCore);
        inOrder.verify(executor).removeAll();
        inOrder.verify(lifecycleDependentComponentManager).onDestroy();
        inOrder.verify(appMetricaServiceCore).onDestroy();
    }

    @Test
    public void updateCallback() {
        AppMetricaServiceCallback callback = mock(AppMetricaServiceCallback.class);
        appMetricaCoreExecutionDispatcher.updateCallback(callback);
        verify(appMetricaServiceCore).updateCallback(callback);
    }
}
