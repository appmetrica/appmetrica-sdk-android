package io.appmetrica.analytics.impl.crash.client.converter;

import io.appmetrica.analytics.impl.crash.ndk.NativeCrashHandlerDescription;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashModel;
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashSource;
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid;
import io.appmetrica.analytics.protobuf.nano.MessageNano;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

public class NativeCrashConverterTest extends CommonTest {

    private NativeCrashConverter converter;

    @Before
    public void setUp() {
        converter = new NativeCrashConverter();
    }

    @Test
    public void conversionToProto() throws IllegalAccessException {
        byte[] nativeCrashValue = new byte[800 * 1024];
        new Random().nextBytes(nativeCrashValue);

        NativeCrashSource handler = NativeCrashSource.CRASHPAD;
        String version = "someVersion";
        CrashAndroid.Crash crash = converter.fromModel(
                new NativeCrashModel(
                        nativeCrashValue, new NativeCrashHandlerDescription(handler, version)
                )
        );

        ObjectPropertyAssertions(crash)
                .withFinalFieldOnly(false)
                .withIgnoredFields("native_")
                .checkField("throwable", (Object) null)
                .checkField("threads", (Object) null)
                .checkField("methodCallStacktrace", new CrashAndroid.StackTraceElement[0])
                .checkField("buildId", "")
                .checkField("isOffline", CrashAndroid.OPTIONAL_BOOL_UNDEFINED)
                .checkField("type", CrashAndroid.Crash.NATIVE)
                .checkField("virtualMachine", "JVM".getBytes())
                .checkField("virtualMachineVersion", "".getBytes())
                .checkField("pluginEnvironment", new CrashAndroid.BytesPair[0])
                .checkAll();

        ObjectPropertyAssertions(crash.native_)
                .withFinalFieldOnly(false)
                .withIgnoredFields("handler")
                .checkField("nativeCrashPayload", nativeCrashValue)
                .checkAll();

        ObjectPropertyAssertions(crash.native_.handler)
                .withFinalFieldOnly(false)
                .checkField("source", CrashAndroid.CRASHPAD)
                .checkField("version", version)
                .checkAll();
    }

    @Test
    public void allNulls() throws IllegalAccessException {
        CrashAndroid.Crash crash = converter.fromModel(
                new NativeCrashModel(
                        new byte[] {},
                        new NativeCrashHandlerDescription(
                                NativeCrashSource.UNKNOWN, null
                        )
                )
        );

        //check for nulls
        MessageNano.toByteArray(crash);

        ObjectPropertyAssertions(crash)
                .withFinalFieldOnly(false)
                .withIgnoredFields("native_")
                .checkField("throwable", (Object) null)
                .checkField("threads", (Object) null)
                .checkField("methodCallStacktrace", new CrashAndroid.StackTraceElement[0])
                .checkField("buildId", "")
                .checkField("isOffline", CrashAndroid.OPTIONAL_BOOL_UNDEFINED)
                .checkField("type", CrashAndroid.Crash.NATIVE)
                .checkField("virtualMachine", "JVM".getBytes())
                .checkField("virtualMachineVersion", "".getBytes())
                .checkField("pluginEnvironment", new CrashAndroid.BytesPair[0])
                .checkAll();

        ObjectPropertyAssertions(crash.native_)
                .withFinalFieldOnly(false)
                .withIgnoredFields("handler")
                .checkField("nativeCrashPayload", new byte[]{})
                .checkAll();

        ObjectPropertyAssertions(crash.native_.handler)
                .withFinalFieldOnly(false)
                .checkField("source", CrashAndroid.UNKNOWN)
                .checkField("version", "")
                .checkAll();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void doesNotConvertToModel() {
        converter.toModel(new CrashAndroid.Crash());
    }

}
