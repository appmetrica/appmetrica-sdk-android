package io.appmetrica.analytics.impl.crash.jvm.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads;
import io.appmetrica.analytics.impl.crash.jvm.client.ThreadState;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class AllThreadsConverterTest extends CommonTest {

    @Mock
    private ThreadStateConverter threadStateConverter;
    @InjectMocks
    private AllThreadsConverter allThreadsConverter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testToProto() throws IllegalAccessException {
        ThreadState threadStateAffected = mock(ThreadState.class);
        ThreadState threadState1 = mock(ThreadState.class);
        ThreadState threadState2 = mock(ThreadState.class);
        ThreadState threadState3 = mock(ThreadState.class);
        List threads = Arrays.asList(threadState1, threadState2, threadState3);
        String process = "process";
        AllThreads allThreads = new AllThreads(
            threadStateAffected,
            threads,
            process
        );
        CrashAndroid.Thread threadAffected = new CrashAndroid.Thread();
        CrashAndroid.Thread thread1 = new CrashAndroid.Thread();
        CrashAndroid.Thread thread2 = new CrashAndroid.Thread();
        CrashAndroid.Thread thread3 = new CrashAndroid.Thread();
        doReturn(threadAffected).when(threadStateConverter).fromModel(threadStateAffected);
        doReturn(thread1).when(threadStateConverter).fromModel(threadState1);
        doReturn(thread2).when(threadStateConverter).fromModel(threadState2);
        doReturn(thread3).when(threadStateConverter).fromModel(threadState3);

        ObjectPropertyAssertions<CrashAndroid.AllThreads> assertions = ObjectPropertyAssertions(allThreadsConverter.fromModel(allThreads));
        assertions.withFinalFieldOnly(false)
            .checkField("affectedThread", threadAffected)
            .checkField("threads", new CrashAndroid.Thread[]{thread1, thread2, thread3})
            .checkField("processName", process)
            .checkAll();

        verify(threadStateConverter).fromModel(threadState1);
        verify(threadStateConverter).fromModel(threadState2);
        verify(threadStateConverter).fromModel(threadState3);
    }

    @Test
    public void testNulls() throws IllegalAccessException {
        AllThreads allThreads = new AllThreads(
            null,
            null,
            null
        );

        ObjectPropertyAssertions<CrashAndroid.AllThreads> assertions = ObjectPropertyAssertions(allThreadsConverter.fromModel(allThreads));
        assertions.withFinalFieldOnly(false)
            .checkField("affectedThread", (Object) null)
            .checkField("threads", new CrashAndroid.Thread[0])
            .checkField("processName", "")
            .checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToModel() {
        allThreadsConverter.toModel(new CrashAndroid.AllThreads());
    }

}
