package io.appmetrica.analytics.impl.utils;

import android.content.Context;
import android.content.Intent;
import io.appmetrica.analytics.coreapi.internal.backport.BiConsumer;
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

@RunWith(RobolectricTestRunner.class)
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
        runnableCaptor.getValue().run();
        verify(block).consume(context, intent);
    }
}
