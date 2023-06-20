package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationState;
import io.appmetrica.analytics.coreapi.internal.servicecomponents.applicationstate.ApplicationStateObserver;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ApplicationStateProviderImplTest extends CommonTest {

    @Mock
    private ApplicationStateObserver mFirstObserver;
    @Mock
    private ApplicationStateObserver mSecondObserver;

    private ApplicationStateProviderImpl mApplicationStateProvider;

    private static final int FIRST_PID = 1;
    private static final int SECOND_PID = 2;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        mApplicationStateProvider = new ApplicationStateProviderImpl();
    }

    @Test
    public void testStateBeforeInit() {
        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.UNKNOWN);
    }

    @Test
    public void testInitialState() {
        mApplicationStateProvider.onCreate();

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.UNKNOWN);
    }

    @Test
    public void testStateForSinglePausedClientSession() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.pauseUserSessionForPid(FIRST_PID);

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.BACKGROUND);
    }

    @Test
    public void testStateForSingleClientSession() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.VISIBLE);
    }

    @Test
    public void testStateForSingleFinishedClientSession() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.pauseUserSessionForPid(FIRST_PID);

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.BACKGROUND);
    }

    @Test
    public void testStateForSingleDisconnectedClient() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.notifyProcessDisconnected(FIRST_PID);

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.UNKNOWN);
    }

    @Test
    public void testStateForTwoResumedClients() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.resumeUserSessionForPid(SECOND_PID);

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.VISIBLE);
    }

    @Test
    public void testStateForOneResumedAndOnePausedClients() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.resumeUserSessionForPid(SECOND_PID);
        mApplicationStateProvider.pauseUserSessionForPid(FIRST_PID);

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.VISIBLE);
    }

    @Test
    public void testStateForOneResumedAndOneDisconnectedClients() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.resumeUserSessionForPid(SECOND_PID);
        mApplicationStateProvider.notifyProcessDisconnected(FIRST_PID);

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.VISIBLE);
    }

    @Test
    public void testStateForTwoPausedClients() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.resumeUserSessionForPid(SECOND_PID);
        mApplicationStateProvider.pauseUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.pauseUserSessionForPid(SECOND_PID);

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.BACKGROUND);
    }

    @Test
    public void testStateForTwoDisconnectedClients() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.resumeUserSessionForPid(SECOND_PID);
        mApplicationStateProvider.notifyProcessDisconnected(FIRST_PID);
        mApplicationStateProvider.notifyProcessDisconnected(SECOND_PID);

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.UNKNOWN);
    }

    @Test
    public void testStateForDisconnectTwoPausedClients() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.resumeUserSessionForPid(SECOND_PID);
        mApplicationStateProvider.pauseUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.pauseUserSessionForPid(SECOND_PID);
        mApplicationStateProvider.notifyProcessDisconnected(FIRST_PID);
        mApplicationStateProvider.notifyProcessDisconnected(SECOND_PID);

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.BACKGROUND);
    }

    @Test
    public void testStateForOnePausedAndOneDisconnectedClients() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.resumeUserSessionForPid(SECOND_PID);
        mApplicationStateProvider.pauseUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.notifyProcessDisconnected(SECOND_PID);

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.BACKGROUND);
    }

    @Test
    public void testRegisterStickyObserverReturnActualState() {
        mApplicationStateProvider.onCreate();

        assertThat(mApplicationStateProvider.registerStickyObserver(mFirstObserver))
                .isEqualTo(ApplicationState.UNKNOWN);
    }

    @Test
    public void testRegisterNullStickyObserverReturnActualState() {
        mApplicationStateProvider.onCreate();

        assertThat(mApplicationStateProvider.registerStickyObserver(null))
                .isEqualTo(ApplicationState.UNKNOWN);
    }

    @Test
    public void testDispatchAppStateUpdateToAllObserversOnClientResumed() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.registerStickyObserver(mFirstObserver);
        mApplicationStateProvider.registerStickyObserver(mSecondObserver);
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);

        verify(mFirstObserver).onApplicationStateChanged(ApplicationState.VISIBLE);
        verify(mSecondObserver).onApplicationStateChanged(ApplicationState.VISIBLE);
    }

    @Test
    public void testDispatchAppStateUpdateToAllObserversOnClientPaused() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.registerStickyObserver(mFirstObserver);
        mApplicationStateProvider.registerStickyObserver(mSecondObserver);
        mApplicationStateProvider.pauseUserSessionForPid(FIRST_PID);

        verify(mFirstObserver).onApplicationStateChanged(ApplicationState.BACKGROUND);
        verify(mSecondObserver).onApplicationStateChanged(ApplicationState.BACKGROUND);
    }

    @Test
    public void testDispatchAppStateUpdateToAllObserversOnClientDisconnected() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.registerStickyObserver(mFirstObserver);
        mApplicationStateProvider.registerStickyObserver(mSecondObserver);
        mApplicationStateProvider.notifyProcessDisconnected(FIRST_PID);

        verify(mFirstObserver).onApplicationStateChanged(ApplicationState.UNKNOWN);
        verify(mSecondObserver).onApplicationStateChanged(ApplicationState.UNKNOWN);
    }

    @Test
    public void testDispatchAppStateIfSecondClientConnected() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.registerStickyObserver(mFirstObserver);
        mApplicationStateProvider.registerStickyObserver(mSecondObserver);
        mApplicationStateProvider.resumeUserSessionForPid(SECOND_PID);

        verify(mFirstObserver, never()).onApplicationStateChanged(any(ApplicationState.class));
        verify(mSecondObserver, never()).onApplicationStateChanged(any(ApplicationState.class));
    }

    @Test
    public void testDispatchAppStateIfOneOfClientsPaused() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.resumeUserSessionForPid(SECOND_PID);
        mApplicationStateProvider.registerStickyObserver(mFirstObserver);
        mApplicationStateProvider.registerStickyObserver(mSecondObserver);
        mApplicationStateProvider.pauseUserSessionForPid(FIRST_PID);

        verify(mFirstObserver, never()).onApplicationStateChanged(any(ApplicationState.class));
        verify(mSecondObserver, never()).onApplicationStateChanged(any(ApplicationState.class));
    }

    @Test
    public void testDispatchAppStateIfOneOfClientsDisconnected() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.resumeUserSessionForPid(SECOND_PID);
        mApplicationStateProvider.registerStickyObserver(mFirstObserver);
        mApplicationStateProvider.registerStickyObserver(mSecondObserver);
        mApplicationStateProvider.notifyProcessDisconnected(FIRST_PID);

        verify(mFirstObserver, never()).onApplicationStateChanged(any(ApplicationState.class));
        verify(mSecondObserver, never()).onApplicationStateChanged(any(ApplicationState.class));
    }

    @Test
    public void testDispatchAppStateOfOneDisconnectedAndOnePaused() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.resumeUserSessionForPid(SECOND_PID);
        mApplicationStateProvider.registerStickyObserver(mFirstObserver);
        mApplicationStateProvider.registerStickyObserver(mSecondObserver);
        mApplicationStateProvider.pauseUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.notifyProcessDisconnected(SECOND_PID);

        verify(mFirstObserver).onApplicationStateChanged(ApplicationState.BACKGROUND);
        verify(mSecondObserver).onApplicationStateChanged(ApplicationState.BACKGROUND);
    }

    @Test
    public void testDispatchQueueOfAppStateChanges() {
        mApplicationStateProvider.onCreate();
        mApplicationStateProvider.registerStickyObserver(mFirstObserver);
        mApplicationStateProvider.resumeUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.pauseUserSessionForPid(FIRST_PID);
        mApplicationStateProvider.resumeUserSessionForPid(SECOND_PID);
        mApplicationStateProvider.notifyProcessDisconnected(SECOND_PID);

        InOrder inOrder = inOrder(mFirstObserver);
        inOrder.verify(mFirstObserver).onApplicationStateChanged(ApplicationState.VISIBLE);
        inOrder.verify(mFirstObserver).onApplicationStateChanged(ApplicationState.BACKGROUND);
        inOrder.verify(mFirstObserver).onApplicationStateChanged(ApplicationState.VISIBLE);
        inOrder.verify(mFirstObserver).onApplicationStateChanged(ApplicationState.BACKGROUND);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testDestroy() {
        mApplicationStateProvider.registerStickyObserver(mFirstObserver);
        mApplicationStateProvider.notifyProcessDisconnected(FIRST_PID);
        mApplicationStateProvider.onDestroy();

        assertThat(mApplicationStateProvider.getCurrentState())
                .isEqualTo(ApplicationState.UNKNOWN);
    }
}
