package io.appmetrica.analytics.impl.crash.utils;

import io.appmetrica.analytics.coreapi.internal.backport.BiFunction;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.ThreadState;
import io.appmetrica.analytics.impl.utils.ProcessDetector;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@RunWith(RobolectricTestRunner.class)
public class ThreadsStateDumperTest extends CommonTest {

    @Mock
    private ThreadsStateDumper.ThreadProvider threadProvider;
    private BiFunction<Thread, StackTraceElement[], ThreadState> threadConverter = mock(BiFunction.class);
    @Mock
    private ProcessDetector processDetector;

    @InjectMocks
    private ThreadsStateDumper threadsStateDumper;

    private Thread mainThread = spy(new Thread("main"));
    @Mock
    private ThreadState mainThreadState;// = mock(ThreadState.class);

    private Map<Thread, StackTraceElement[]> otherThreads = new LinkedHashMap<Thread, StackTraceElement[]>();

    private List<ThreadState> otherThreadState = new ArrayList<ThreadState>();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        for (int i = 0; i < 10; i++) {
            Thread key = new Thread(String.valueOf(i));
            StackTraceElement[] value = new StackTraceElement[0];
            otherThreads.put(key, value);
            ThreadState threadState = mock(ThreadState.class);
            otherThreadState.add(threadState);
            doReturn(threadState).when(threadConverter).apply(key, value);
        }
        doReturn(mainThreadState).when(threadConverter).apply(same(mainThread), any(StackTraceElement[].class));
        doReturn(mainThread).when(threadProvider).getMainThread();
        doReturn(otherThreads).when(threadProvider).getAllOtherThreads();
    }

    @Test
    public void testAnr() {
        String processName = "processName";
        doReturn(processName).when(processDetector).getProcessName();

        AllThreads allThreads = threadsStateDumper.getThreadsDumpForAnr();
        SoftAssertions soft = new SoftAssertions();

        soft.assertThat(allThreads.affectedThread).as("main thread").isSameAs(mainThreadState);
        soft.assertThat(allThreads.threads).as("other threads").containsExactlyElementsOf(otherThreadState);
        soft.assertThat(allThreads.processName).as("processName").isEqualTo(processName);

        soft.assertAll();
    }

    @Test
    public void testDumpForCrash() {
        Thread excluded = new ArrayList<Thread>(otherThreads.keySet()).get(5);
        List<ThreadState> withoutExcluded = new ArrayList<ThreadState>(otherThreadState);
        withoutExcluded.remove(5);
        withoutExcluded.add(mainThreadState);

        List<ThreadState> forCrash = threadsStateDumper.getThreadsDumpForCrash(excluded);
        assertThat(forCrash).containsOnlyElementsOf(withoutExcluded);
    }

}
