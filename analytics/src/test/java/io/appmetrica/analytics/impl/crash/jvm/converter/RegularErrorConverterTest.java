package io.appmetrica.analytics.impl.crash.jvm.converter;

import io.appmetrica.analytics.assertions.ProtoObjectPropertyAssertions;
import io.appmetrica.analytics.impl.crash.jvm.client.AllThreads;
import io.appmetrica.analytics.impl.crash.jvm.client.RegularError;
import io.appmetrica.analytics.impl.crash.jvm.client.StackTraceItemInternal;
import io.appmetrica.analytics.impl.crash.jvm.client.ThrowableModel;
import io.appmetrica.analytics.impl.crash.jvm.client.UnhandledException;
import io.appmetrica.analytics.impl.crash.jvm.converter.AllThreadsConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.CrashOptionalBoolConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.PlatformConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.PluginEnvironmentConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.RegularErrorConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.StackTraceConverter;
import io.appmetrica.analytics.impl.crash.jvm.converter.ThrowableConverter;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid.Error.DEFAULT;
import static io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid.OPTIONAL_BOOL_TRUE;
import static io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid.OPTIONAL_BOOL_UNDEFINED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class RegularErrorConverterTest extends CommonTest {

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
    private RegularErrorConverter regularErrorConverter;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        regularErrorConverter = new RegularErrorConverter(
                throwableConverter,
                allThreadsConverter,
                optionalBoolConverter,
                stackTraceConverter,
                platformConverter,
                pluginEnvironmentConverter
        );
    }

    @Test
    public void testToProto() throws IllegalAccessException, IOException {
        String message = "some message";
        ThrowableModel throwableModel = mock(ThrowableModel.class);
        CrashAndroid.Throwable protoThrowable = mock(CrashAndroid.Throwable.class);
        when(throwableConverter.fromModel(throwableModel)).thenReturn(protoThrowable);
        AllThreads allThreadsModel = mock(AllThreads.class);
        CrashAndroid.AllThreads protoAllThreads = mock(CrashAndroid.AllThreads.class);
        when(allThreadsConverter.fromModel(allThreadsModel)).thenReturn(protoAllThreads);
        List<StackTraceItemInternal> stacktraceModel = Arrays.asList(mock(StackTraceItemInternal.class));
        CrashAndroid.StackTraceElement[] protoStacktrace = new CrashAndroid.StackTraceElement[] { mock(CrashAndroid.StackTraceElement.class) };
        when(stackTraceConverter.fromModel(stacktraceModel)).thenReturn(protoStacktrace);
        String platform = "unity";
        byte[] protoPlatform = "proto platform".getBytes();
        when(platformConverter.fromModel(platform)).thenReturn(protoPlatform);
        String virtualMachineVersion = "33.22.44";
        Map<String, String> environmentModel = new HashMap<>();
        environmentModel.put("key", "value");
        CrashAndroid.BytesPair[] protoEnvironment = new CrashAndroid.BytesPair[] { mock(CrashAndroid.BytesPair.class)};
        when(pluginEnvironmentConverter.fromModel(environmentModel)).thenReturn(protoEnvironment);
        String buildId = "5555-6666";
        when(optionalBoolConverter.toProto(true)).thenReturn(OPTIONAL_BOOL_TRUE);
        RegularError error = new RegularError(message, new UnhandledException(
                throwableModel,
                allThreadsModel,
                stacktraceModel,
                platform,
                virtualMachineVersion,
                environmentModel,
                buildId,
                true
        ));

        new ProtoObjectPropertyAssertions<>(regularErrorConverter.fromModel(error))
                .checkField("message", message)
                .checkFieldIsNull("custom")
                .checkField("throwable", protoThrowable)
                .checkField("threads", protoAllThreads)
                .checkField("virtualMachine", protoPlatform)
                .checkField("virtualMachineVersion", virtualMachineVersion.getBytes())
                .checkField("methodCallStacktrace", protoStacktrace)
                .checkField("pluginEnvironment", protoEnvironment)
                .checkField("buildId", buildId)
                .checkField("isOffline", OPTIONAL_BOOL_TRUE)
                .checkField("type", DEFAULT)
                .checkAll();
    }

    @Test
    public void toProtoWithNulls() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        when(optionalBoolConverter.toProto(null)).thenReturn(OPTIONAL_BOOL_UNDEFINED);
        RegularError error = new RegularError(null, null);

        new ProtoObjectPropertyAssertions<>(regularErrorConverter.fromModel(error))
                .checkField("message", "")
                .checkFieldIsNull("throwable")
                .checkFieldsAreNull("threads", "custom")
                .checkField("virtualMachine", "JVM".getBytes())
                .checkField("virtualMachineVersion", "".getBytes())
                .checkField("methodCallStacktrace", new CrashAndroid.StackTraceElement[0])
                .checkField("pluginEnvironment", new CrashAndroid.BytesPair[0])
                .checkField("buildId", "")
                .checkField("isOffline", OPTIONAL_BOOL_UNDEFINED)
                .checkField("type", DEFAULT)
                .checkAll();

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testToModel() {
        regularErrorConverter.toModel(new CrashAndroid.Error());
    }

    @Test
    public void messageWithInvalidEncoding() throws Throwable {
        String prefix = "identifier";
        String invalidFormattedStringWithUnpairedSurrogate = "\uD83D";
        RegularError regularError = new RegularError(
            prefix + invalidFormattedStringWithUnpairedSurrogate,
            null
        );
        CrashAndroid.Error protoError = regularErrorConverter.fromModel(regularError);
        byte[] protoBytes = MessageNano.toByteArray(protoError);
        assertThat(CrashAndroid.Error.parseFrom(protoBytes).message).contains(prefix);
    }
}
