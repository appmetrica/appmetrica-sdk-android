package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent;
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityLifecycleListener;
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
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ActivityLifecycleManagerTest extends CommonTest {

    @NonNull
    private final ActivityLifecycleManager activityLifecycleManager = new ActivityLifecycleManager();
    @Mock
    private Application application;
    @Mock
    private ActivityLifecycleListener firstListener;
    @Mock
    private ActivityLifecycleListener secondListener;
    @Mock
    private ActivityLifecycleListener thirdListener;
    @Mock
    private ActivityLifecycleListener fourthListener;
    @Mock
    private ActivityLifecycleListener fifthListener;
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
        activityLifecycleManager.registerListener(firstListener, ActivityEvent.CREATED);
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
        activityLifecycleManager.registerListener(firstListener, ActivityEvent.CREATED);
        verify(application).registerActivityLifecycleCallbacks(activityLifecycleManager);
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
        clearInvocations(application);
        activityLifecycleManager.maybeInit(application);
        verify(application, never()).registerActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
    }

    @Test
    public void maybeInitFirstContextThenApplication() {
        activityLifecycleManager.registerListener(firstListener, ActivityEvent.CREATED);
        activityLifecycleManager.maybeInit(context);
        clearInvocations(application);
        activityLifecycleManager.maybeInit(application);
        verify(application, never()).registerActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
    }

    @Test
    public void maybeInitFirstApplicationThenContext() {
        activityLifecycleManager.registerListener(firstListener, ActivityEvent.CREATED);
        activityLifecycleManager.maybeInit(application);
        clearInvocations(application);
        activityLifecycleManager.maybeInit(context);
        verify(application, never()).registerActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
    }

    @Test
    public void maybeInitBadContext() {
        Context context = mock(Context.class);
        activityLifecycleManager.registerListener(firstListener, ActivityEvent.CREATED);
        activityLifecycleManager.maybeInit(context);
        verify(context).getApplicationContext();
        verifyNoMoreInteractions(context);
        verifyNoMoreInteractions(application);
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.NO_APPLICATION);
    }

    @Test
    public void unregisterListenerNotWatching() {
        activityLifecycleManager.registerListener(firstListener, ActivityEvent.CREATED);
        activityLifecycleManager.unregisterListener(firstListener, ActivityEvent.CREATED);
        verifyNoMoreInteractions(application);
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.NO_APPLICATION);
    }

    @Test
    public void unregisterListenerHasMoreListeners() {
        activityLifecycleManager.maybeInit(application);
        activityLifecycleManager.registerListener(firstListener, ActivityEvent.CREATED);
        activityLifecycleManager.registerListener(secondListener, ActivityEvent.CREATED);
        activityLifecycleManager.unregisterListener(firstListener, ActivityEvent.CREATED);
        verify(application, never()).unregisterActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
    }

    @Test
    public void unregisterListenerHasThisListenerForAnotherEvent() {
        activityLifecycleManager.maybeInit(application);
        activityLifecycleManager.registerListener(
                firstListener,
                ActivityEvent.CREATED,
                ActivityEvent.RESUMED
        );
        activityLifecycleManager.unregisterListener(firstListener, ActivityEvent.CREATED);
        verify(application, never()).unregisterActivityLifecycleCallbacks(any(Application.ActivityLifecycleCallbacks.class));
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
    }

    @Test
    public void unregisterListenerShouldUnregister() {
        activityLifecycleManager.maybeInit(application);
        activityLifecycleManager.registerListener(firstListener, ActivityEvent.CREATED);
        activityLifecycleManager.unregisterListener(firstListener, ActivityEvent.CREATED);
        verify(application).unregisterActivityLifecycleCallbacks(activityLifecycleManager);
        assertThat(activityLifecycleManager.getWatchingStatus()).isEqualTo(ActivityLifecycleManager.WatchingStatus.NOT_WATCHING_YET);
    }

    @Test
    public void listenersNotification() {
        activityLifecycleManager.maybeInit(application);
        activityLifecycleManager.registerListener(
                firstListener,
                ActivityEvent.CREATED,
                ActivityEvent.RESUMED
        );
        activityLifecycleManager.registerListener(secondListener, ActivityEvent.PAUSED);
        activityLifecycleManager.registerListener(thirdListener, ActivityEvent.RESUMED);
        activityLifecycleManager.registerListener(
                fourthListener,
                ActivityEvent.STARTED,
                ActivityEvent.STOPPED,
                ActivityEvent.DESTROYED
        );
        activityLifecycleManager.registerListener(fifthListener);

        activityLifecycleManager.onActivityCreated(activity, null);
        verify(firstListener).onEvent(activity, ActivityEvent.CREATED);
        verify(fifthListener).onEvent(activity, ActivityEvent.CREATED);
        verifyNoMoreInteractions(secondListener, thirdListener, fourthListener);
        clearInvocations(firstListener, secondListener, thirdListener, fourthListener, fifthListener);
        activityLifecycleManager.onActivityResumed(activity);
        verify(firstListener).onEvent(activity, ActivityEvent.RESUMED);
        verify(thirdListener).onEvent(activity, ActivityEvent.RESUMED);
        verify(fifthListener).onEvent(activity, ActivityEvent.RESUMED);
        verifyNoMoreInteractions(secondListener, fourthListener);
        clearInvocations(firstListener, secondListener, thirdListener, fourthListener, fifthListener);
        activityLifecycleManager.onActivityPaused(activity);
        verify(secondListener).onEvent(activity, ActivityEvent.PAUSED);
        verify(fifthListener).onEvent(activity, ActivityEvent.PAUSED);
        verifyNoMoreInteractions(firstListener, thirdListener, fourthListener);
        clearInvocations(firstListener, secondListener, thirdListener, fourthListener, fifthListener);

        activityLifecycleManager.onActivityStarted(activity);
        verify(fourthListener).onEvent(activity, ActivityEvent.STARTED);
        verify(fifthListener).onEvent(activity, ActivityEvent.STARTED);
        verifyNoMoreInteractions(firstListener, secondListener, thirdListener);
        clearInvocations(firstListener, secondListener, thirdListener, fourthListener, fifthListener);

        activityLifecycleManager.onActivityStopped(activity);
        verify(fourthListener).onEvent(activity, ActivityEvent.STOPPED);
        verify(fifthListener).onEvent(activity, ActivityEvent.STOPPED);
        verifyNoMoreInteractions(firstListener, secondListener, thirdListener);
        clearInvocations(firstListener, secondListener, thirdListener, fourthListener, fifthListener);

        activityLifecycleManager.onActivityDestroyed(activity);
        verify(fourthListener).onEvent(activity, ActivityEvent.DESTROYED);
        verify(fifthListener).onEvent(activity, ActivityEvent.DESTROYED);
        verifyNoMoreInteractions(firstListener, secondListener, thirdListener);
        clearInvocations(firstListener, secondListener, thirdListener, fourthListener, fifthListener);

        activityLifecycleManager.unregisterListener(firstListener, ActivityEvent.RESUMED);
        activityLifecycleManager.onActivityResumed(activity);
        verifyNoMoreInteractions(firstListener);
        verify(thirdListener).onEvent(activity, ActivityEvent.RESUMED);
        clearInvocations(firstListener, thirdListener);
        activityLifecycleManager.onActivityCreated(activity, null);
        verify(firstListener).onEvent(activity, ActivityEvent.CREATED);

        activityLifecycleManager.unregisterListener(fourthListener);
        activityLifecycleManager.onActivityStopped(activity);
        verifyNoMoreInteractions(fourthListener);
    }
}
