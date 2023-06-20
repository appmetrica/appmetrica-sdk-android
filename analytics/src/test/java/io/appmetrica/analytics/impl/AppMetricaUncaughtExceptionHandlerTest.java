package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.CrashProcessor;
import io.appmetrica.analytics.impl.crash.client.ICrashProcessor;
import io.appmetrica.analytics.impl.crash.client.ThreadState;
import io.appmetrica.analytics.impl.crash.utils.CrashedThreadConverter;
import io.appmetrica.analytics.impl.crash.utils.ThreadsStateDumper;
import io.appmetrica.analytics.impl.utils.ProcessDetector;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class AppMetricaUncaughtExceptionHandlerTest extends CommonTest {

    @Mock
    private Throwable mThrowableMock;
    @Mock
    private Thread mThreadMock;
    @Mock
    private ProcessDetector processDetector;
    @Mock
    private CrashedThreadConverter crashedThreadConverter;
    @Mock
    private ThreadsStateDumper threadsStateDumper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUncaughtExceptionShouldDispatchThrowableToDefaultHandler() {
        Thread.UncaughtExceptionHandler defaultHandlerMock = mock(Thread.UncaughtExceptionHandler.class);
        AppMetricaUncaughtExceptionHandler appMetricaUncaughtExceptionHandler = new AppMetricaUncaughtExceptionHandler(
                defaultHandlerMock,
                new ArrayList<ICrashProcessor>(),
                processDetector,
                crashedThreadConverter,
                threadsStateDumper
        );
        appMetricaUncaughtExceptionHandler.uncaughtException(mThreadMock, mThrowableMock);

        ArgumentCaptor<Thread> arg1 = ArgumentCaptor.forClass(Thread.class);
        ArgumentCaptor<Throwable> arg2 = ArgumentCaptor.forClass(Throwable.class);
        verify(defaultHandlerMock, times(1)).uncaughtException(arg1.capture(), arg2.capture());

        assertThat(arg1.getValue()).isEqualTo(mThreadMock);
        assertThat(arg2.getValue()).isEqualTo(mThrowableMock);
    }

    @Test
    public void testUncaughtExceptionShouldNotThrowExceptionIfDefaultHandlerIsNotDefined() {
        AppMetricaUncaughtExceptionHandler appMetricaUncaughtExceptionHandler = new AppMetricaUncaughtExceptionHandler(
                null,
                new ArrayList<ICrashProcessor>(),
                processDetector,
                crashedThreadConverter,
                threadsStateDumper
        );
        appMetricaUncaughtExceptionHandler.uncaughtException(mThreadMock, mThrowableMock);
    }

    @Test
    public void testUncaughtExceptionExceptionShouldDispatchThrowableToAllProcessors() throws IllegalAccessException {
        final String processName = "processName";
        doReturn(processName).when(processDetector).getProcessName();

        List<ICrashProcessor> crashProcessors = new ArrayList<ICrashProcessor>();
        for (int i = 0; i < 5; i++) {
            crashProcessors.add(mock(CrashProcessor.class));
        }
        AppMetricaUncaughtExceptionHandler appMetricaUncaughtExceptionHandler = new AppMetricaUncaughtExceptionHandler(
                null,
                crashProcessors,
                processDetector,
                crashedThreadConverter,
                threadsStateDumper
        );
        ThreadState threadState = mock(ThreadState.class);
        when(crashedThreadConverter.apply(mThreadMock)).thenReturn(threadState);
        List<ThreadState> threads = Arrays.asList(mock(ThreadState.class));
        when(threadsStateDumper.getThreadsDumpForCrash(mThreadMock)).thenReturn(threads);
        appMetricaUncaughtExceptionHandler.uncaughtException(mThreadMock, mThrowableMock);
        for (ICrashProcessor crashProcessor : crashProcessors) {
            ArgumentCaptor<AllThreads> arg = ArgumentCaptor.forClass(AllThreads.class);
            verify(crashProcessor, times(1)).processCrash(same(mThrowableMock), arg.capture());
            ObjectPropertyAssertions(arg.getValue())
                    .checkField("processName", processName)
                    .checkField("threads", threads)
                    .checkField("affectedThread", threadState)
                    .checkAll();
        }
    }
}
