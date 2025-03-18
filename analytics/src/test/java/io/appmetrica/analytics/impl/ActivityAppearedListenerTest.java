package io.appmetrica.analytics.impl;

import android.app.Activity;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ActivityAppearedListenerTest extends CommonTest {

    @Rule
    public ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

    @Mock
    private ActivityLifecycleManager activityLifecycleManager;
    @Mock
    private ActivityAppearedListener.Listener firstListener;
    @Mock
    private ActivityAppearedListener.Listener secondListener;
    @Mock
    private Activity activity;
    @Mock
    private IHandlerExecutor executor;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;
    private ActivityAppearedListener activityAppearedListener;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor()).thenReturn(executor);
        activityAppearedListener = new ActivityAppearedListener(activityLifecycleManager);
    }

    @Test
    public void listenerRegistered() {
        verify(activityLifecycleManager).registerListener(activityAppearedListener);
    }

    @Test
    public void onActivityAppeared() {
        activityAppearedListener.registerListener(firstListener);
        activityAppearedListener.registerListener(secondListener);
        activityAppearedListener.onActivityAppeared(activity);
        verify(firstListener).onActivityAppeared(activity);
        verify(secondListener).onActivityAppeared(activity);
    }

    @Test
    public void onEvent() {
        activityAppearedListener.registerListener(firstListener);
        activityAppearedListener.registerListener(secondListener);
        activityAppearedListener.onEvent(activity, ActivityEvent.CREATED);
        verifyNoInteractions(firstListener, secondListener);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(firstListener).onActivityAppeared(activity);
        verify(secondListener).onActivityAppeared(activity);
    }
}
