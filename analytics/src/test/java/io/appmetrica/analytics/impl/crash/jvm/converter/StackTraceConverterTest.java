package io.appmetrica.analytics.impl.crash.jvm.converter;

import io.appmetrica.analytics.impl.crash.jvm.client.StackTraceItemInternal;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class StackTraceConverterTest extends CommonTest {

    @Mock
    private StackTraceElementConverter elementConverter;
    @InjectMocks
    private StackTraceConverter stackTraceConverter = new StackTraceConverter();

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testToProto() {
        StackTraceItemInternal element1 = mock(StackTraceItemInternal.class);
        StackTraceItemInternal element2 = mock(StackTraceItemInternal.class);
        List<StackTraceItemInternal> list = Arrays.asList(element1, element2);
        CrashAndroid.StackTraceElement outElement1 = mock(CrashAndroid.StackTraceElement.class);
        CrashAndroid.StackTraceElement outElement2 = mock(CrashAndroid.StackTraceElement.class);
        doReturn(outElement1).when(elementConverter).fromModel(element1);
        doReturn(outElement2).when(elementConverter).fromModel(element2);
        assertThat(stackTraceConverter.fromModel(list)).containsExactly(outElement1, outElement2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToModel() {
        stackTraceConverter.toModel(CrashAndroid.StackTraceElement.emptyArray());
    }

}
