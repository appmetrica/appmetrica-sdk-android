package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ActivityLifecycleManagerTest extends CommonTest {

    @NonNull
    private final ActivityLifecycleManager activityLifecycleManager = new ActivityLifecycleManager();
    @Mock
    private Application application;
    @Mock
    private ActivityLifecycleManager.Listener firstListener;
    @Mock
    private ActivityLifecycleManager.Listener secondListener;
    @Mock
    private ActivityLifecycleManager.Listener thirdListener;
    @Mock
    private ActivityLifecycleManager.Listener fourthListener;
    @Mock
    private ActivityLifecycleManager.Listener fifthListener;
    @Mock
    private Activity activity;
    private Context context;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(application.getApplicationContext()).thenReturn(application);
        context = application;
    }

    @Test
    public void maybeInitWithContext() {
        activityLifecycleManager.maybeInit(context);
        verify(application, never()).registerActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.NOT_WATCHING_YET);
        activityLifecycleManager.registerListener(firstListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        verify(application).registerActivityLifecycleCallbacks(activityLifecycleManager);
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
        clearInvocations(application);
        activityLifecycleManager.maybeInit(context);
        verify(application, never()).registerActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
    }

    @Test
    public void maybeInitWithApplication() {
        activityLifecycleManager.maybeInit(application);
        verify(application, never()).registerActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.NOT_WATCHING_YET);
        activityLifecycleManager.registerListener(firstListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        verify(application).registerActivityLifecycleCallbacks(activityLifecycleManager);
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
        clearInvocations(application);
        activityLifecycleManager.maybeInit(application);
        verify(application, never()).registerActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
    }

    @Test
    public void maybeInitFirstContextThenApplication() {
        activityLifecycleManager.registerListener(firstListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        activityLifecycleManager.maybeInit(context);
        clearInvocations(application);
        activityLifecycleManager.maybeInit(application);
        verify(application, never()).registerActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
    }

    @Test
    public void maybeInitFirstApplicationThenContext() {
        activityLifecycleManager.registerListener(firstListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        activityLifecycleManager.maybeInit(application);
        clearInvocations(application);
        activityLifecycleManager.maybeInit(context);
        verify(application, never()).registerActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
    }

    @Test
    public void maybeInitBadContext() {
        Context context = mock(Context.class);
        activityLifecycleManager.registerListener(firstListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        activityLifecycleManager.maybeInit(context);
        verify(context).getApplicationContext();
        verifyNoMoreInteractions(context);
        verifyZeroInteractions(application);
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.NO_APPLICATION);
    }

    @Test
    public void unregisterListenerNotWatching() {
        activityLifecycleManager.registerListener(firstListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        activityLifecycleManager.unregisterListener(firstListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        verifyZeroInteractions(application);
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.NO_APPLICATION);
    }

    @Test
    public void unregisterListenerHasMoreListeners() {
        activityLifecycleManager.maybeInit(application);
        activityLifecycleManager.registerListener(firstListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        activityLifecycleManager.registerListener(secondListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        activityLifecycleManager.unregisterListener(firstListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        verify(application, never()).unregisterActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
    }

    @Test
    public void unregisterListenerHasThisListenerForAnotherEvent() {
        activityLifecycleManager.maybeInit(application);
        activityLifecycleManager.registerListener(
                firstListener,
                ActivityLifecycleManager.ActivityEvent.CREATED,
                ActivityLifecycleManager.ActivityEvent.RESUMED
        );
        activityLifecycleManager.unregisterListener(firstListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        verify(application, never()).unregisterActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
    }

    @Test
    public void unregisterListenerShouldUnregister() {
        activityLifecycleManager.maybeInit(application);
        activityLifecycleManager.registerListener(firstListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        activityLifecycleManager.unregisterListener(firstListener, ActivityLifecycleManager.ActivityEvent.CREATED);
        verify(application).unregisterActivityLifecycleCallbacks(activityLifecycleManager);
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.NOT_WATCHING_YET);
    }

    @Test
    public void listenersNotification() {
        activityLifecycleManager.maybeInit(application);
        activityLifecycleManager.registerListener(
                firstListener,
                ActivityLifecycleManager.ActivityEvent.CREATED,
                ActivityLifecycleManager.ActivityEvent.RESUMED
        );
        activityLifecycleManager.registerListener(secondListener, ActivityLifecycleManager.ActivityEvent.PAUSED);
        activityLifecycleManager.registerListener(thirdListener, ActivityLifecycleManager.ActivityEvent.RESUMED);
        activityLifecycleManager.registerListener(
                fourthListener,
                ActivityLifecycleManager.ActivityEvent.STARTED,
                ActivityLifecycleManager.ActivityEvent.STOPPED,
                ActivityLifecycleManager.ActivityEvent.DESTROYED
        );
        activityLifecycleManager.registerListener(fifthListener);

        activityLifecycleManager.onActivityCreated(activity, null);
        verify(firstListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.CREATED);
        verify(fifthListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.CREATED);
        verifyZeroInteractions(secondListener, thirdListener, fourthListener);
        clearInvocations(firstListener, secondListener, thirdListener, fourthListener, fifthListener);
        activityLifecycleManager.onActivityResumed(activity);
        verify(firstListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.RESUMED);
        verify(thirdListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.RESUMED);
        verify(fifthListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.RESUMED);
        verifyZeroInteractions(secondListener, fourthListener);
        clearInvocations(firstListener, secondListener, thirdListener, fourthListener, fifthListener);
        activityLifecycleManager.onActivityPaused(activity);
        verify(secondListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.PAUSED);
        verify(fifthListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.PAUSED);
        verifyZeroInteractions(firstListener, thirdListener, fourthListener);
        clearInvocations(firstListener, secondListener, thirdListener, fourthListener, fifthListener);

        activityLifecycleManager.onActivityStarted(activity);
        verify(fourthListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.STARTED);
        verify(fifthListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.STARTED);
        verifyZeroInteractions(firstListener, secondListener, thirdListener);
        clearInvocations(firstListener, secondListener, thirdListener, fourthListener, fifthListener);

        activityLifecycleManager.onActivityStopped(activity);
        verify(fourthListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.STOPPED);
        verify(fifthListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.STOPPED);
        verifyZeroInteractions(firstListener, secondListener, thirdListener);
        clearInvocations(firstListener, secondListener, thirdListener, fourthListener, fifthListener);

        activityLifecycleManager.onActivityDestroyed(activity);
        verify(fourthListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.DESTROYED);
        verify(fifthListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.DESTROYED);
        verifyZeroInteractions(firstListener, secondListener, thirdListener);
        clearInvocations(firstListener, secondListener, thirdListener, fourthListener, fifthListener);

        activityLifecycleManager.unregisterListener(firstListener, ActivityLifecycleManager.ActivityEvent.RESUMED);
        activityLifecycleManager.onActivityResumed(activity);
        verifyZeroInteractions(firstListener);
        verify(thirdListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.RESUMED);
        clearInvocations(firstListener, thirdListener);
        activityLifecycleManager.onActivityCreated(activity, null);
        verify(firstListener).onEvent(activity, ActivityLifecycleManager.ActivityEvent.CREATED);

        activityLifecycleManager.unregisterListener(fourthListener);
        activityLifecycleManager.onActivityStopped(activity);
        verifyZeroInteractions(fourthListener);
    }
}
