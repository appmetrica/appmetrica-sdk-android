package io.appmetrica.analytics.impl.crash.client.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.crash.client.AllThreads;
import io.appmetrica.analytics.impl.crash.client.Anr;
import io.appmetrica.analytics.impl.crash.client.ThreadState;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class AnrConverterTest extends CommonTest {

    @Mock
    private AllThreadsConverter allThreadsConverter;
    @Mock
    private CrashOptionalBoolConverter optionalBoolConverter;
    private AnrConverter anrConverter = new AnrConverter();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        anrConverter = new AnrConverter(allThreadsConverter, optionalBoolConverter);
    }

    @Test
    public void testToProto() throws IllegalAccessException {
        ThreadState mainThread = mock(ThreadState.class);
        AllThreads state = new AllThreads(
                mainThread,
                Arrays.asList(mock(ThreadState.class)),
                "process"
        );
        String buildId = "buildId";
        Boolean isOffline = true;
        CrashAndroid.AllThreads allThreads = new CrashAndroid.AllThreads();
        int protoIsOffline = CrashAndroid.OPTIONAL_BOOL_TRUE;
        doReturn(allThreads).when(allThreadsConverter).fromModel(state);
        doReturn(protoIsOffline).when(optionalBoolConverter).toProto(isOffline);

        ObjectPropertyAssertions<CrashAndroid.Anr> assertions
                = ObjectPropertyAssertions(anrConverter.fromModel(new Anr(state, buildId, isOffline)));
        assertions.withFinalFieldOnly(false);
        assertions.checkField("threads", allThreads);
        assertions.checkField("buildId", buildId);
        assertions.checkField("isOffline", protoIsOffline);
        assertions.checkAll();
    }

    @Test
    public void testToProtoNullable() throws IllegalAccessException {
        ThreadState mainThread = mock(ThreadState.class);
        AllThreads state = new AllThreads(
                mainThread,
                Arrays.asList(mock(ThreadState.class)),
                null
        );
        CrashAndroid.AllThreads allThreads = new CrashAndroid.AllThreads();
        doReturn(allThreads).when(allThreadsConverter).fromModel(state);
        doReturn(CrashAndroid.OPTIONAL_BOOL_UNDEFINED).when(optionalBoolConverter).toProto((Boolean) any());

        ObjectPropertyAssertions<CrashAndroid.Anr> assertions
                = ObjectPropertyAssertions(anrConverter.fromModel(new Anr(state, null, null)));
        assertions.withFinalFieldOnly(false);
        assertions.checkField("threads", allThreads);
        assertions.checkField("buildId", "");
        assertions.checkField("isOffline", CrashAndroid.OPTIONAL_BOOL_UNDEFINED);
        assertions.checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToModel() {
        anrConverter.toModel(new CrashAndroid.Anr());
    }

}
