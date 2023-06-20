package io.appmetrica.analytics.impl;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Process;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppAppMetricaServiceLifecycleTests extends CommonTest {

    private static final String ACTION_CLIENT_CONNECTION = "io.appmetrica.analytics.IAppMetricaService";
    private static final String ACTION_COLLECT_BG_LOCATION = "io.appmetrica.analytics.ACTION_C_BG_L";

    @Mock
    private AppMetricaServiceLifecycle.LifecycleObserver mObserver;
    @Mock
    private AppMetricaServiceLifecycle.LifecycleObserver mObserver2;
    @Mock
    private AppMetricaServiceLifecycle.LifecycleObserver mObserver3;
    @Mock
    private AppMetricaServiceLifecycle.LifecycleObserver mObserver4;
    @Mock
    private AppMetricaServiceLifecycle.LifecycleObserver mObserver5;
    @Mock
    private Configuration configuration;

    private AppMetricaServiceLifecycle mAppMetricaServiceLifecycle;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mAppMetricaServiceLifecycle = new AppMetricaServiceLifecycle();
    }

    @Test
    public void testOnCreateInvolvingOnAllObservers() {
        fillAllObservers();
        mAppMetricaServiceLifecycle.onCreate();
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testOnStartInvolvingOnAllObservers() {
        fillAllObservers();
        mAppMetricaServiceLifecycle.onStart(mock(Intent.class), 0);
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testOnStartCommandInvolvingOnAllObservers() {
        fillAllObservers();
        mAppMetricaServiceLifecycle.onStartCommand(mock(Intent.class), 0, 0);
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testOnDestroyInvolvingOnAllObservers() {
        fillAllObservers();
        mAppMetricaServiceLifecycle.onDestroy();
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void onConfigurationChanged() {
        fillAllObservers();
        mAppMetricaServiceLifecycle.onConfigurationChanged(configuration);
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    private void fillAllObservers() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnBindClientAction() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientAction();
        mAppMetricaServiceLifecycle.onBind(intent);
        verify(mObserver, times(1)).onEvent(intent);
    }

    @Test
    public void testFirstClientConnectObserverOnBindMetricaClientAction() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnBindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testFirstClientConnectObserverOnRebindClientAction() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientAction();
        mAppMetricaServiceLifecycle.onRebind(intent);
        verify(mObserver, times(1)).onEvent(intent);
    }

    @Test
    public void testFirstClientConnectObserverOnRebindMetricaClientAction() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnRebindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onRebind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testFirstClientConnectObserverOnRepeatedBindClientAction() {
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientAction());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientAction());
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testFirstClientConnectObserverOnRepeatedBindMetricaClientAction() {
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnRepeatedBindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnBindMetricaClientActionAfterNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnBindNonMetricaClientActionAfterMetricaClientAction() {
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testFirstClientConnectObserverOnRepeatedRebindClientAction() {
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientAction());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientAction());
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testFirstClientConnectObserverOnRepeatedRebindMetricaClientAction() {
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnRepeatedRebindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnRebindMetricaClientActionAfterNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnRebindNonMetricaClientActionAfterMetricaClientAction() {
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onRebind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testFirstClientConnectObserverOnBindMetricaClientActionAfterRebindMetricaClientAction() {
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnRebindMetricaClientActionAfterBindMetricaClientAction() {
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnBindNonMetricaClientActionAfterRebindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnRebindNonMetricaClientActionAfterBindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnBindNonMetricaClientActionAfterRebindMetricaClientAction() {
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testFirstClientConnectObserverOnBindMetricaClientActionAfterRebindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnRebindNonMetricaClientActionAfterBindMetricaClientAction() {
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onRebind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testFirstClientConnectObserverOnRebindMetricaClientActionAfterBindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess());
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnBindLocationAction() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(intentWithAction(ACTION_COLLECT_BG_LOCATION));
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testFirstClientConnectObserverOnRebindLocationAction() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(intentWithAction(ACTION_COLLECT_BG_LOCATION));
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testFirstClientConnectObserverOnUnbindClientAction() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientAction());
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testFirstClientConnectObserverOnUnbindMetricaClientAction() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnUnbindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndNonMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testFirstClientConnectObserverOnUnbindLocationAction() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onUnbind(intentWithAction(ACTION_COLLECT_BG_LOCATION));
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testNewClientConnectObserverOnBindClientAction() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientAction();
        mAppMetricaServiceLifecycle.onBind(intent);
        verify(mObserver, times(1)).onEvent(intent);
    }

    @Test
    public void testNewClientConnectObserverOnBindMetricaClientAction() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testNewClientConnectObserverOnBindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testNewClientConnectObserverOnRebindClientAction() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientAction();
        mAppMetricaServiceLifecycle.onRebind(intent);
        verify(mObserver, times(1)).onEvent(intent);
    }

    @Test
    public void testNewClientConnectObserverOnRebindMetricaClientAction() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testNewClientConnectObserverOnRebindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onRebind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testNewClientConnectObserverOnRepeatedBindClientAction() {
        Intent intent = prepareIntentWithClientAction();
        mAppMetricaServiceLifecycle.onBind(intent);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(intent);
        verify(mObserver, times(1)).onEvent(intent);
    }

    @Test
    public void testNewClientConnectObserverOnRepeatedBindMetricaClientAction() {
        Intent intent = prepareIntentWithClientActionAndMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(intent);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testNewClientConnectObserverOnRepeatedBindNonMetricaClientAction() {
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testNewClientConnectObserverOnRepeatedRebindClientAction() {
        Intent intent = prepareIntentWithClientAction();
        mAppMetricaServiceLifecycle.onRebind(intent);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(intent);
        verify(mObserver, times(1)).onEvent(intent);
    }

    @Test
    public void testNewClientConnectObserverOnRepeatedRebindMetricaClientAction() {
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testNewClientConnectObserverOnRepeatedRebindNonMetricaClientAction() {
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onRebind(intent);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testNewClientConnectObserverOnBindClientActionTwice() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientAction();
        mAppMetricaServiceLifecycle.onBind(intent);
        mAppMetricaServiceLifecycle.onBind(intent);
        verify(mObserver, times(2)).onEvent(intent);
    }

    @Test
    public void testNewClientConnectObserverOnBindMetricaClientActionTwice() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);
        mAppMetricaServiceLifecycle.onBind(intent);
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testNewClientConnectObserverOnBindNonMetricaClientActionTwice() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);
        mAppMetricaServiceLifecycle.onBind(intent);
        verify(mObserver, times(2)).onEvent(intent);
    }

    @Test
    public void testNewClientConnectObserverOnRebindClientActionTwice() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientAction();
        mAppMetricaServiceLifecycle.onRebind(intent);
        mAppMetricaServiceLifecycle.onRebind(intent);
        verify(mObserver, times(2)).onEvent(intent);
    }

    @Test
    public void testNewClientConnectObserverOnRebindMetricaClientActionTwice() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testNewClientConnectObserverOnRebindNonMetricaClientActionTwice() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onRebind(intent);
        mAppMetricaServiceLifecycle.onRebind(intent);
        verify(mObserver, times(2)).onEvent(intent);
    }

    @Test
    public void testNewClientConnectObserverOnBindLocationAction() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(intentWithAction(ACTION_COLLECT_BG_LOCATION));
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testNewClientConnectObserverOnRebindLocationAction() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(intentWithAction(ACTION_COLLECT_BG_LOCATION));
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testNewClientConnectObserverOnUnbindClientAction() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientAction());
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testNewClientConnectObserverOnUnbindMetricaClientAction() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testNewClientConnectObserverOnUnbindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndNonMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testNewClientConnectObserverOnUnbindLocationAction() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.onUnbind(intentWithAction(ACTION_COLLECT_BG_LOCATION));
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testAllClientDisconnectObserverOnBindClientAction() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientAction());
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testAllClientDisconnectObserverOnBindMetricaClientAction() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testAllClientDisconnectObserverOnBindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testAllClientDisconnectObserverOnRebindClientAction() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientAction());
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testAllClientDisconnectObserverOnRebindMetricaClientAction() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testAllClientDisconnectObserverOnRebindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testAllClientDisconnectObserverOnBindLocationAction() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(intentWithAction(ACTION_COLLECT_BG_LOCATION));
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testAllClientDisconnectObserverOnRebindLocationAction() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(intentWithAction(ACTION_COLLECT_BG_LOCATION));
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testAllClientDisconnectObserverOnUnbindLocationAction() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onUnbind(intentWithAction(ACTION_COLLECT_BG_LOCATION));
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testAllClientDisconnectObserverOnUnbindClientAction() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        Intent intent = prepareIntentWithClientAction();
        mAppMetricaServiceLifecycle.onUnbind(intent);
        verify(mObserver, times(1)).onEvent(intent);
    }

    @Test
    public void testAllClientDisconnectObserverOnUnbindMetricaClientAction() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testAllClientDisconnectObserverOnUnbindNonMetricaClientAction() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onUnbind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testAllClientDisconnectObserverOnUnbindAllClientsAfterBind() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        Intent intent = prepareIntentWithClientAction();
        mAppMetricaServiceLifecycle.onBind(intent);
        mAppMetricaServiceLifecycle.onUnbind(intent);
        verify(mObserver, times(1)).onEvent(intent);
    }

    @Test
    public void testAllClientDisconnectObserverOnUnbindAllMetricaClientsAfterBind() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testAllClientDisconnectObserverOnUnbindAllNonMetricaClientsAfterBind() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);
        mAppMetricaServiceLifecycle.onUnbind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testAllClientDisconnectObserverOnUnbindAllClientsAfterRebind() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        Intent intent = prepareIntentWithClientAction();
        mAppMetricaServiceLifecycle.onRebind(intent);
        mAppMetricaServiceLifecycle.onUnbind(intent);
        verify(mObserver, times(1)).onEvent(intent);
    }

    @Test
    public void testAllClientDisconnectObserverOnUnbindAllMetricaClientsAfterRebind() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testAllClientDisconnectObserverOnUnbindAllNonMetricaClientsAfterRebind() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);
        mAppMetricaServiceLifecycle.onUnbind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testAllClientDisconnectObserverOnPartiallyUnbind() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientAction());
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientAction());
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientAction());
        verify(mObserver, never()).onEvent(any(Intent.class));
    }

    @Test
    public void testAllClientDisconnectObserverOnPartiallyUnbindMetricaClients() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testAllClientDisconnectObserverOnPartiallyUnbindNonMetricaClients() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess());
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess());
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndNonMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testAllClientDisconnectObserverOnTotallyUnbind() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        Intent intent = prepareIntentWithClientAction();
        mAppMetricaServiceLifecycle.onBind(intent);
        mAppMetricaServiceLifecycle.onRebind(intent);
        mAppMetricaServiceLifecycle.onUnbind(intent);
        mAppMetricaServiceLifecycle.onUnbind(intent);
        verify(mObserver, times(1)).onEvent(intent);
    }

    @Test
    public void testAllClientDisconnectObserverOnTotallyUnbindMetricaClients() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testAllClientDisconnectObserverOnTotallyUnbindNonMetricaClients() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);
        mAppMetricaServiceLifecycle.onRebind(intent);
        mAppMetricaServiceLifecycle.onUnbind(intent);
        mAppMetricaServiceLifecycle.onUnbind(intent);
        verify(mObserver).onEvent(intent);
    }

    @Test
    public void testAllClientDisconnectObserverOnTotallyUnbindOnlyMetricaClients() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.onBind(prepareIntentWithClientActionAndNonMetricaProcess());
        mAppMetricaServiceLifecycle.onRebind(prepareIntentWithClientActionAndNonMetricaProcess());
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndMetricaProcess());
        mAppMetricaServiceLifecycle.onUnbind(prepareIntentWithClientActionAndNonMetricaProcess());
        verifyZeroInteractions(mObserver);
    }

    @Test
    public void testAllClientDisconnectObserverOnTotallyUnbindOnlyNonMetricaClients() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        Intent nonMetricaIntent = prepareIntentWithClientActionAndNonMetricaProcess();
        Intent metricaIntent = prepareIntentWithClientActionAndMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(metricaIntent);
        mAppMetricaServiceLifecycle.onRebind(metricaIntent);
        mAppMetricaServiceLifecycle.onBind(nonMetricaIntent);
        mAppMetricaServiceLifecycle.onRebind(nonMetricaIntent);
        mAppMetricaServiceLifecycle.onUnbind(metricaIntent);
        mAppMetricaServiceLifecycle.onUnbind(nonMetricaIntent);
        mAppMetricaServiceLifecycle.onUnbind(nonMetricaIntent);
        verify(mObserver).onEvent(nonMetricaIntent);
    }

    @Test
    public void testFirstClientConnectObserverNotifyingOrder() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver2);
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver3);
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver4);
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver5);

        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);

        verifyObserversNotifyingOrder(intent);
    }

    @Test
    public void testNewClientConnectObserverNotifyingOrder() {
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver2);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver3);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver4);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver5);

        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);

        verifyObserversNotifyingOrder(intent);
    }

    @Test
    public void testAllClientDisconnectedObserverNotifyingOrder() {
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver);
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver2);
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver3);
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver4);
        mAppMetricaServiceLifecycle.addAllClientDisconnectedObserver(mObserver5);

        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onUnbind(intent);

        verifyObserversNotifyingOrder(intent);
    }

    @Test
    public void testClientObserversNotifyingOrder() {
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver2);
        mAppMetricaServiceLifecycle.addFirstClientConnectObserver(mObserver3);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver4);
        mAppMetricaServiceLifecycle.addNewClientConnectObserver(mObserver5);

        Intent intent = prepareIntentWithClientActionAndNonMetricaProcess();
        mAppMetricaServiceLifecycle.onBind(intent);

        verifyObserversNotifyingOrder(intent);
    }

    private void verifyObserversNotifyingOrder(Intent intent) {
        InOrder inOrder = inOrder(mObserver, mObserver2, mObserver3, mObserver4, mObserver5);
        inOrder.verify(mObserver).onEvent(intent);
        inOrder.verify(mObserver2).onEvent(intent);
        inOrder.verify(mObserver3).onEvent(intent);
        inOrder.verify(mObserver4).onEvent(intent);
        inOrder.verify(mObserver5).onEvent(intent);
    }

    private Intent intentWithAction(String action) {
        Intent intent = mock(Intent.class);
        when(intent.getAction()).thenReturn(action);
        return intent;
    }

    private Intent prepareIntentWithClientAction() {
        return intentWithAction(ACTION_CLIENT_CONNECTION);
    }

    private Intent prepareIntentWithClientActionAndMetricaProcess() {
        return prepareIntentWithWithActionAndPid(ACTION_CLIENT_CONNECTION, Process.myPid());
    }

    private Intent prepareIntentWithClientActionAndNonMetricaProcess() {
        return prepareIntentWithWithActionAndPid(ACTION_CLIENT_CONNECTION, Process.myPid() + 1);
    }

    private Intent prepareIntentWithWithActionAndPid(String action, int pid) {
        Intent intent = mock(Intent.class);
        when(intent.getAction()).thenReturn(action);
        when(intent.getData())
                .thenReturn(
                        new Uri.Builder()
                                .scheme("metrica")
                                .authority("com.yandex.test.package.name")
                                .path("client")
                                .appendQueryParameter("pid", String.valueOf(pid))
                                .build()
                );
        return intent;
    }
}
