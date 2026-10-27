package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import android.content.Intent;
import io.appmetrica.analytics.coreapi.internal.backport.BiConsumer;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class BackgroundBroadcastReceiverTest extends CommonTest {

    @Mock
    private Context context;
    @Mock
    private Intent intent;
    @Mock
    private BiConsumer<Context, Intent> block;
    @Mock
    private ICommonExecutor executor;
    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;
    private BackgroundBroadcastReceiver backgroundBroadcastReceiver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        backgroundBroadcastReceiver = new BackgroundBroadcastReceiver(block, executor);
    }

    @Test
    public void onReceive() {
        backgroundBroadcastReceiver.onReceive(context, intent);
        verify(executor).execute(runnableCaptor.capture());
        verify(executor, never()).post(any(Runnable.class));
        verifyNoInteractions(block);
        runnableCaptor.getValue().run();
        verify(block).consume(same(context), same(intent));
    }

    @Test
    public void callbackExceptionIsNotWrapped() {
        RuntimeException exception = new RuntimeException();
        doThrow(exception).when(block).consume(context, intent);

        backgroundBroadcastReceiver.onReceive(context, intent);
        verify(executor).execute(runnableCaptor.capture());

        assertThatThrownBy(() -> runnableCaptor.getValue().run()).isSameAs(exception);
    }
}
