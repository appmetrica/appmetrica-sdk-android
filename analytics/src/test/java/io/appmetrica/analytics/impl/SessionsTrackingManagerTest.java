package io.appmetrica.analytics.impl;

import android.app.Activity;
import io.appmetrica.analytics.impl.utils.ConditionalExecutor;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SessionsTrackingManagerTest extends CommonTest {

    @Mock
    private ActivityLifecycleManager activityLifecycleManager;
    @Mock
    private ConditionalExecutor<MainReporter> conditionalExecutor;
    @Mock
    private Activity activity;
    @Mock
    private MainReporter mainReporter;
    @Mock
    private ActivityStateManager activityStateManager;
    @Mock
    private ActivityAppearedListener activityAppearedListener;
    @Captor
    private ArgumentCaptor<ActivityLifecycleManager.Listener> listenerCaptor;
    @Captor
    private ArgumentCaptor<NonNullConsumer<MainReporter>> commandCaptor;
    private SessionsTrackingManager sessionsTrackingManager;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionsTrackingManager = new SessionsTrackingManager(
                activityLifecycleManager,
                activityAppearedListener,
                conditionalExecutor,
                activityStateManager
        );
    }

    @Test
    public void startWatchingAutoStartedReportsEvent() {
        when(activityLifecycleManager.getWatchingStatus()).thenReturn(ActivityLifecycleManager.WatchingStatus.WATCHING);
        assertThat(sessionsTrackingManager.startWatching(true)).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
    }

    @Test
    public void startWatchingManualStartedReportsEvent() {
        when(activityLifecycleManager.getWatchingStatus()).thenReturn(ActivityLifecycleManager.WatchingStatus.WATCHING);
        assertThat(sessionsTrackingManager.startWatching(false)).isEqualTo(ActivityLifecycleManager.WatchingStatus.WATCHING);
    }

    @Test
    public void startWatchingAutoNotStartedDoesNotReportEvent() {
        when(activityLifecycleManager.getWatchingStatus()).thenReturn(ActivityLifecycleManager.WatchingStatus.NO_APPLICATION);
        assertThat(sessionsTrackingManager.startWatching(true)).isEqualTo(ActivityLifecycleManager.WatchingStatus.NO_APPLICATION);
    }

    @Test
    public void startWatchingManualNotStartedDoesNotReportEvent() {
        when(activityLifecycleManager.getWatchingStatus()).thenReturn(ActivityLifecycleManager.WatchingStatus.NO_APPLICATION);
        assertThat(sessionsTrackingManager.startWatching(false)).isEqualTo(ActivityLifecycleManager.WatchingStatus.NO_APPLICATION);
    }

    @Test
    public void resumeActivityManuallyStateChanged() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED))
                .thenReturn(true);
        sessionsTrackingManager.resumeActivityManually(activity, mainReporter);
        InOrder inOrder = Mockito.inOrder(activityAppearedListener, mainReporter);
        inOrder.verify(activityAppearedListener).onActivityAppeared(activity);
        inOrder.verify(mainReporter).resumeSession(activity);
    }

    @Test
    public void resumeNullActivityManuallyStateChanged() {
        when(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.RESUMED))
                .thenReturn(true);
        sessionsTrackingManager.resumeActivityManually(null, mainReporter);
        verify(mainReporter).resumeSession(null);
        verify(activityAppearedListener, never()).onActivityAppeared(nullable(Activity.class));
    }

    @Test
    public void resumeActivityManuallyStateDidNotChange() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED))
                .thenReturn(false);
        sessionsTrackingManager.resumeActivityManually(activity, mainReporter);
        verify(activityAppearedListener).onActivityAppeared(activity);
        verifyZeroInteractions(mainReporter);
    }

    @Test
    public void pauseActivityManuallyStateChanged() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.PAUSED))
                .thenReturn(true);
        sessionsTrackingManager.pauseActivityManually(activity, mainReporter);
        InOrder inOrder = Mockito.inOrder(activityAppearedListener, mainReporter);
        inOrder.verify(activityAppearedListener).onActivityAppeared(activity);
        inOrder.verify(mainReporter).pauseSession(activity);
    }

    @Test
    public void pauseNullActivityManuallyStateChanged() {
        when(activityStateManager.didStateChange(null, ActivityStateManager.ActivityState.PAUSED))
                .thenReturn(true);
        sessionsTrackingManager.pauseActivityManually(null, mainReporter);
        verify(mainReporter).pauseSession(null);
        verify(activityAppearedListener, never()).onActivityAppeared(nullable(Activity.class));
    }

    @Test
    public void pauseActivityManuallyStateDidNotChange() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.PAUSED))
                .thenReturn(false);
        sessionsTrackingManager.pauseActivityManually(activity, mainReporter);
        verifyZeroInteractions(mainReporter);
        verify(activityAppearedListener).onActivityAppeared(activity);
    }

    @Test
    public void setReporter() {
        sessionsTrackingManager.setReporter(mainReporter);
        verify(conditionalExecutor).setResource(mainReporter);
    }

    @Test
    public void sessionResumedStateChanged() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED)).thenReturn(true);
        sessionsTrackingManager.startWatching(true);
        verify(activityLifecycleManager).registerListener(listenerCaptor.capture(), eq(ActivityLifecycleManager.ActivityEvent.RESUMED));
        listenerCaptor.getValue().onEvent(activity, ActivityLifecycleManager.ActivityEvent.RESUMED);
        verify(conditionalExecutor).addCommand(commandCaptor.capture());
        commandCaptor.getValue().consume(mainReporter);
        verify(mainReporter).resumeSession(activity);
        verifyNoInteractions(activityAppearedListener);
    }

    @Test
    public void sessionResumedStateDidNotChange() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.RESUMED)).thenReturn(false);
        sessionsTrackingManager.startWatching(true);
        verify(activityLifecycleManager).registerListener(listenerCaptor.capture(), eq(ActivityLifecycleManager.ActivityEvent.RESUMED));
        listenerCaptor.getValue().onEvent(activity, ActivityLifecycleManager.ActivityEvent.RESUMED);
        verify(conditionalExecutor).addCommand(commandCaptor.capture());
        commandCaptor.getValue().consume(mainReporter);
        verifyZeroInteractions(mainReporter);
        verifyNoInteractions(activityAppearedListener);
    }

    @Test
    public void sessionPausedStateChanged() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.PAUSED)).thenReturn(true);
        sessionsTrackingManager.startWatching(true);
        verify(activityLifecycleManager).registerListener(listenerCaptor.capture(), eq(ActivityLifecycleManager.ActivityEvent.PAUSED));
        listenerCaptor.getValue().onEvent(activity, ActivityLifecycleManager.ActivityEvent.PAUSED);
        verify(conditionalExecutor).addCommand(commandCaptor.capture());
        commandCaptor.getValue().consume(mainReporter);
        verify(mainReporter).pauseSession(activity);
        verifyNoInteractions(activityAppearedListener);
    }

    @Test
    public void sessionPausedStateDidNotChange() {
        when(activityStateManager.didStateChange(activity, ActivityStateManager.ActivityState.PAUSED)).thenReturn(false);
        sessionsTrackingManager.startWatching(true);
        verify(activityLifecycleManager).registerListener(listenerCaptor.capture(), eq(ActivityLifecycleManager.ActivityEvent.PAUSED));
        listenerCaptor.getValue().onEvent(activity, ActivityLifecycleManager.ActivityEvent.PAUSED);
        verify(conditionalExecutor).addCommand(commandCaptor.capture());
        commandCaptor.getValue().consume(mainReporter);
        verifyZeroInteractions(mainReporter);
        verifyNoInteractions(activityAppearedListener);
    }
}
