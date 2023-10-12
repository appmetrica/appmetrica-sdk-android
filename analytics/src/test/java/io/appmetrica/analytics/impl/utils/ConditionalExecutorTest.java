package io.appmetrica.analytics.impl.utils;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.coreapi.internal.executors.ICommonExecutor;
import io.appmetrica.analytics.impl.MainReporter;
import io.appmetrica.analytics.impl.NonNullConsumer;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(RobolectricTestRunner.class)
public class ConditionalExecutorTest extends CommonTest {

    @Mock
    private MainReporter reporter;
    @Mock
    private ICommonExecutor executor;
    private ConditionalExecutor<MainReporter> conditionalExecutor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        conditionalExecutor = new ConditionalExecutor<MainReporter>(executor);
    }

    @Test
    public void addCommands() {
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        conditionalExecutor.addCommand(new NonNullConsumer<MainReporter>() {
            @Override
            public void consume(@NonNull MainReporter data) {
                data.resumeSession();
            }
        });
        conditionalExecutor.addCommand(new NonNullConsumer<MainReporter>() {
            @Override
            public void consume(@NonNull MainReporter data) {
                data.pauseSession();
            }
        });
        verify(executor, times(2)).execute(runnableCaptor.capture());
        List<Runnable> addCommandRunnables = runnableCaptor.getAllValues();
        for (Runnable runnable : addCommandRunnables) {
            runnable.run();
        }
        verifyNoMoreInteractions(reporter);

        clearInvocations(executor);
        conditionalExecutor.setResource(reporter);
        verifyNoMoreInteractions(executor);
        InOrder inOrder = Mockito.inOrder(reporter);
        inOrder.verify(reporter).resumeSession();
        inOrder.verify(reporter).pauseSession();

        clearInvocations(reporter);
        runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        conditionalExecutor.addCommand(new NonNullConsumer<MainReporter>() {
            @Override
            public void consume(@NonNull MainReporter data) {
                data.enableAnrMonitoring();
            }
        });
        verify(executor).execute(runnableCaptor.capture());
        List<Runnable> newAddCommandRunnables = runnableCaptor.getAllValues();
        assertThat(newAddCommandRunnables.size()).isEqualTo(1);
        newAddCommandRunnables.get(0).run();
        verify(reporter).enableAnrMonitoring();
    }
}
