package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.content.Intent;
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

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppOpenWatcherTest extends CommonTest {

    @Mock
    private ICommonExecutor apiProxyExecutor;
    @Mock
    private Activity activity;
    @Mock
    private Intent intent;
    @Mock
    private DeeplinkConsumer deeplinkConsumer;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;
    private final String deeplink = "some deeplink";
    @Mock
    private ActivityLifecycleManager activityLifecycleManager;
    private AppOpenWatcher appOpenWatcher;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(activity.getIntent()).thenReturn(intent);
        when(intent.getDataString()).thenReturn(deeplink);
        appOpenWatcher = new AppOpenWatcher(apiProxyExecutor, activityLifecycleManager);
    }

    @Test
    public void startWatching() {
        appOpenWatcher.startWatching();
        verify(activityLifecycleManager).registerListener(appOpenWatcher, ActivityLifecycleManager.ActivityEvent.CREATED);
    }

    @Test
    public void hasCallbacksNoDeeplinkConsumer() {
        appOpenWatcher.startWatching();
        appOpenWatcher.onEvent(activity, ActivityLifecycleManager.ActivityEvent.CREATED);
        verifyNoMoreInteractions(apiProxyExecutor, deeplinkConsumer);
        appOpenWatcher.setDeeplinkConsumer(deeplinkConsumer);
        verifyNoMoreInteractions(apiProxyExecutor);
        verify(deeplinkConsumer).reportAutoAppOpen(deeplink);
    }

    @Test
    public void hasCallbacksHasDeeplinkConsumer() {
        appOpenWatcher.startWatching();
        appOpenWatcher.setDeeplinkConsumer(deeplinkConsumer);
        clearInvocations(apiProxyExecutor);
        appOpenWatcher.onEvent(activity, ActivityLifecycleManager.ActivityEvent.CREATED);
        verify(apiProxyExecutor).execute(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(deeplinkConsumer).reportAutoAppOpen(deeplink);
    }

    @Test
    public void hasCallbacksHasDeeplinkConsumerNoIntent() {
        when(activity.getIntent()).thenReturn(null);
        appOpenWatcher.startWatching();
        appOpenWatcher.setDeeplinkConsumer(deeplinkConsumer);
        appOpenWatcher.onEvent(activity, ActivityLifecycleManager.ActivityEvent.CREATED);
        verifyNoMoreInteractions(apiProxyExecutor, deeplinkConsumer);
    }

    @Test
    public void hasCallbacksHasDeeplinkConsumerGetIntentThrows() {
        when(activity.getIntent()).thenThrow(new RuntimeException());
        appOpenWatcher.startWatching();
        appOpenWatcher.setDeeplinkConsumer(deeplinkConsumer);
        appOpenWatcher.onEvent(activity, ActivityLifecycleManager.ActivityEvent.CREATED);
        verifyNoMoreInteractions(apiProxyExecutor, deeplinkConsumer);
    }

    @Test
    public void hasCallbacksHasDeeplinkConsumerNullIntent() {
        when(activity.getIntent()).thenReturn(null);
        appOpenWatcher.startWatching();
        appOpenWatcher.setDeeplinkConsumer(deeplinkConsumer);
        appOpenWatcher.onEvent(activity, ActivityLifecycleManager.ActivityEvent.CREATED);
        verifyNoMoreInteractions(apiProxyExecutor, deeplinkConsumer);
    }

    @Test
    public void hasCallbacksHasDeeplinkConsumerNullDeeplink() {
        when(intent.getDataString()).thenReturn(null);
        appOpenWatcher.startWatching();
        appOpenWatcher.setDeeplinkConsumer(deeplinkConsumer);
        appOpenWatcher.onEvent(activity, ActivityLifecycleManager.ActivityEvent.CREATED);
        verifyNoMoreInteractions(apiProxyExecutor, deeplinkConsumer);
    }

    @Test
    public void hasCallbacksHasDeeplinkConsumerEmptyDeeplink() {
        when(intent.getDataString()).thenReturn("");
        appOpenWatcher.startWatching();
        appOpenWatcher.setDeeplinkConsumer(deeplinkConsumer);
        appOpenWatcher.onEvent(activity, ActivityLifecycleManager.ActivityEvent.CREATED);
        verifyNoMoreInteractions(apiProxyExecutor, deeplinkConsumer);
    }

}
