package io.appmetrica.analytics.impl;

import android.app.Activity;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(RobolectricTestRunner.class)
public class ActivityAppearedListenerTest extends CommonTest {

    @Mock
    private ICommonExecutor executor;
    @Mock
    private ActivityLifecycleManager activityLifecycleManager;
    @Mock
    private ActivityAppearedListener.Listener firstListener;
    @Mock
    private ActivityAppearedListener.Listener secondListener;
    @Mock
    private Activity activity;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;
    private ActivityAppearedListener activityAppearedListener;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        activityAppearedListener = new ActivityAppearedListener(activityLifecycleManager, executor);
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
        activityAppearedListener.onEvent(activity, ActivityLifecycleManager.ActivityEvent.CREATED);
        verifyNoInteractions(firstListener, secondListener);
        verify(executor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(firstListener).onActivityAppeared(activity);
        verify(secondListener).onActivityAppeared(activity);
    }
}
