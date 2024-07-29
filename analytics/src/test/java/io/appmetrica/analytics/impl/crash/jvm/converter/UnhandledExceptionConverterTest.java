package io.appmetrica.analytics.impl.crash.jvm.converter;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads;
import io.appmetrica.analytics.impl.crash.jvm.client.StackTraceItemInternal;
import io.appmetrica.analytics.impl.crash.jvm.client.ThreadState;
import io.appmetrica.analytics.impl.crash.jvm.client.ThrowableModel;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.impl.crash.jvm.converter.AllThreadsConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.CrashOptionalBoolConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.PlatformConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.PluginEnvironmentConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.StackTraceConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.ThrowableConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.UnhandledExceptionConverter;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.protobuf.nano.InvalidProtocolBufferNanoException;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@RunWith(RobolectricTestRunner.class)
public class UnhandledExceptionConverterTest extends CommonTest {

    @Mock
    private ThrowableConverter throwableConverter;
    @Mock
    private AllThreadsConverter allThreadsConverter;
    @Mock
    private CrashOptionalBoolConverter optionalBoolConverter;
    @Mock
    private StackTraceConverter stackTraceConverter;
    @Mock
    private PlatformConverter platformConverter;
    @Mock
    private PluginEnvironmentConverter pluginEnvironmentConverter;

    private UnhandledExceptionConverter exceptionConverter;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionConverter = new UnhandledExceptionConverter(
                throwableConverter,
                allThreadsConverter,
                optionalBoolConverter,
                stackTraceConverter,
                platformConverter,
                pluginEnvironmentConverter
        );
    }

    @Test
    public void testToProto() throws IllegalAccessException, InvalidProtocolBufferNanoException {
        String platform = "unity";
        String virtualMachineVersion = "3.4.5";
        Map<String, String> environment = new HashMap<>();
        environment.put("key1", "value1");
        environment.put("key2", "22");
        ThreadState crashedThread = mock(ThreadState.class);
        AllThreads allThreads = new AllThreads(crashedThread, Arrays.asList(mock(ThreadState.class)), "process");
        List<StackTraceItemInternal> stacktrace = Collections.singletonList(mock(StackTraceItemInternal.class));
        String buildId = UUID.randomUUID().toString();
        Boolean isOffline = true;
        ThrowableModel rawException = mock(ThrowableModel.class);
        UnhandledException exception = new UnhandledException(
                rawException,
                allThreads,
                stacktrace,
                platform,
                virtualMachineVersion,
                environment,
                buildId,
                isOffline
        );

        CrashAndroid.AllThreads threads = new CrashAndroid.AllThreads();
        doReturn(threads).when(allThreadsConverter).fromModel(allThreads);

        CrashAndroid.Throwable throwable = new CrashAndroid.Throwable();
        doReturn(throwable).when(throwableConverter).fromModel(rawException);

        int protoIsOffline = CrashAndroid.OPTIONAL_BOOL_TRUE;
        doReturn(protoIsOffline).when(optionalBoolConverter).toProto(isOffline);

        CrashAndroid.StackTraceElement[] protoStacktrace = new CrashAndroid.StackTraceElement[1];
        protoStacktrace[0] = mock(CrashAndroid.StackTraceElement.class);
        doReturn(protoStacktrace).when(stackTraceConverter).fromModel(stacktrace);

        byte[] protoPlatform = "some platform".getBytes();
        doReturn(protoPlatform).when(platformConverter).fromModel(platform);

        CrashAndroid.BytesPair[] protoEnv = new CrashAndroid.BytesPair[] { mock(CrashAndroid.BytesPair.class) };
        doReturn(protoEnv).when(pluginEnvironmentConverter).fromModel(environment);

        ObjectPropertyAssertions<CrashAndroid.Crash> crashAssertions = ObjectPropertyAssertions(
                exceptionConverter.fromModel(exception)
        );

        crashAssertions.withFinalFieldOnly(false)
                .withIgnoredFields("type", "native_")
                .checkField("throwable", throwable)
                .checkField("threads", threads)
                .checkField("methodCallStacktrace", protoStacktrace)
                .checkField("buildId", buildId)
                .checkField("isOffline", protoIsOffline)
                .checkField("virtualMachine", protoPlatform)
                .checkField("virtualMachineVersion", virtualMachineVersion.getBytes())
                .checkField("pluginEnvironment", protoEnv)
                .checkAll();

        verify(throwableConverter).fromModel(rawException);
        verify(allThreadsConverter).fromModel(allThreads);
        verify(optionalBoolConverter).toProto(isOffline);
        verify(stackTraceConverter).fromModel(stacktrace);
        verify(platformConverter).fromModel(platform);
        verify(pluginEnvironmentConverter).fromModel(environment);
    }

    @Test
    public void testToProtoWithNulls() throws IllegalAccessException {
        UnhandledException exception = new UnhandledException(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        doReturn(null).when(allThreadsConverter).fromModel(nullable(AllThreads.class));

        int protoIsOffline = CrashAndroid.OPTIONAL_BOOL_UNDEFINED;
        doReturn(protoIsOffline).when(optionalBoolConverter).toProto((Boolean) any());

        CrashAndroid.Crash crash = exceptionConverter.fromModel(exception);
        ObjectPropertyAssertions<CrashAndroid.Crash> crashAssertions = ObjectPropertyAssertions(
                crash
        );
        crashAssertions.withFinalFieldOnly(false)
                .withIgnoredFields("type", "native_")
                .checkFieldIsNull("throwable")
                .checkFieldIsNull("threads")
                .checkField("methodCallStacktrace", new CrashAndroid.StackTraceElement[0])
                .checkField("virtualMachine", "JVM".getBytes())
                .checkField("virtualMachineVersion", "".getBytes())
                .checkField("pluginEnvironment", new CrashAndroid.BytesPair[0])
                .checkField("buildId", "")
                .checkField("isOffline", protoIsOffline)
                .checkAll();

        verifyNoInteractions(allThreadsConverter, throwableConverter, allThreadsConverter);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToModel() {
        exceptionConverter.toModel(new CrashAndroid.Crash());
    }

}
