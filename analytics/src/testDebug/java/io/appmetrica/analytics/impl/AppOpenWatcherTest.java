package io.appmetrica.analytics.impl;

import android.app.Activity;
import android.content.Intent;
import io.appmetrica.analytics.coreapi.internal.executors.IHandlerExecutor;
import io.appmetrica.analytics.coreapi.internal.lifecycle.ActivityEvent;
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AppOpenWatcherTest extends CommonTest {

    @Rule
    public ClientServiceLocatorRule clientServiceLocatorRule = new ClientServiceLocatorRule();

    @Mock
    private IHandlerExecutor executor;
    @Mock
    private Activity activity;
    @Mock
    private Intent intent;
    @Mock
    private IMainReporter reporter;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;
    private final String deeplink = "some deeplink";
    private AppOpenWatcher appOpenWatcher;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(ClientServiceLocator.getInstance().getClientExecutorProvider().getDefaultExecutor()).thenReturn(executor);
        when(activity.getIntent()).thenReturn(intent);
        when(intent.getDataString()).thenReturn(deeplink);
        appOpenWatcher = new AppOpenWatcher();
    }

    @Test
    public void startWatching() {
        appOpenWatcher.startWatching();
        verify(ClientServiceLocator.getInstance().getActivityLifecycleManager())
            .registerListener(appOpenWatcher, ActivityEvent.CREATED);
    }

    @Test
    public void stopWatching() {
        appOpenWatcher.stopWatching();
        verify(ClientServiceLocator.getInstance().getActivityLifecycleManager())
            .unregisterListener(appOpenWatcher, ActivityEvent.CREATED);
    }

    @Test
    public void hasCallbacksNoReporter() {
        appOpenWatcher.startWatching();
        appOpenWatcher.onEvent(activity, ActivityEvent.CREATED);
        verifyNoMoreInteractions(executor, reporter);
        appOpenWatcher.setReporter(reporter);
        verifyNoMoreInteractions(executor);
        verify(reporter).reportAutoAppOpen(deeplink);
    }

    @Test
    public void hasCallbacksHasReporter() {
        appOpenWatcher.startWatching();
        appOpenWatcher.setReporter(reporter);
        clearInvocations(executor);
        appOpenWatcher.onEvent(activity, ActivityEvent.CREATED);
        verify(executor).post(runnableCaptor.capture());
        runnableCaptor.getValue().run();
        verify(reporter).reportAutoAppOpen(deeplink);
    }

    @Test
    public void hasCallbacksHasReporterNoIntent() {
        when(activity.getIntent()).thenReturn(null);
        appOpenWatcher.startWatching();
        appOpenWatcher.setReporter(reporter);
        appOpenWatcher.onEvent(activity, ActivityEvent.CREATED);
        verifyNoMoreInteractions(executor, reporter);
    }

    @Test
    public void hasCallbacksHasReporterGetIntentThrows() {
        when(activity.getIntent()).thenThrow(new RuntimeException());
        appOpenWatcher.startWatching();
        appOpenWatcher.setReporter(reporter);
        appOpenWatcher.onEvent(activity, ActivityEvent.CREATED);
        verifyNoMoreInteractions(executor, reporter);
    }

    @Test
    public void hasCallbacksHasReporterNullIntent() {
        when(activity.getIntent()).thenReturn(null);
        appOpenWatcher.startWatching();
        appOpenWatcher.setReporter(reporter);
        appOpenWatcher.onEvent(activity, ActivityEvent.CREATED);
        verifyNoMoreInteractions(executor, reporter);
    }

    @Test
    public void hasCallbacksHasReporterNullDeeplink() {
        when(intent.getDataString()).thenReturn(null);
        appOpenWatcher.startWatching();
        appOpenWatcher.setReporter(reporter);
        appOpenWatcher.onEvent(activity, ActivityEvent.CREATED);
        verifyNoMoreInteractions(executor, reporter);
    }

    @Test
    public void hasCallbacksHasReporterEmptyDeeplink() {
        when(intent.getDataString()).thenReturn("");
        appOpenWatcher.startWatching();
        appOpenWatcher.setReporter(reporter);
        appOpenWatcher.onEvent(activity, ActivityEvent.CREATED);
        verifyNoMoreInteractions(executor, reporter);
    }

}
