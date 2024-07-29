package io.appmetrica.analytics.impl.crash.jvm.converter

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions
import io.appmetrica.analytics.impl.crash.jvm.converter.NativeCrashConverter
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashHandlerDescription
import io.appmetrica.analytics.impl.crash.ndk.NativeCrashModel
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrashSource
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Test
import java.util.Random

class NativeCrashConverterTest : CommonTest() {
    private val converter by setUp { NativeCrashConverter() }

    @Test
    fun conversionToProto() {
        val nativeCrashValue = ByteArray(800 * 1024).also { Random().nextBytes(it) }
        val handler = NativeCrashSource.CRASHPAD
        val version = "someVersion"
        val crash = converter.fromModel(
            NativeCrashModel(
                nativeCrashValue, NativeCrashHandlerDescription(handler, version)
            )
        )

        ObjectPropertyAssertions(crash)
            .withFinalFieldOnly(false)
            .withIgnoredFields("native_")
            .checkField("throwable", null as Any?)
            .checkField("threads", null as Any?)
            .checkField("methodCallStacktrace", arrayOfNulls<CrashAndroid.StackTraceElement>(0))
            .checkField("buildId", "")
            .checkField("isOffline", CrashAndroid.OPTIONAL_BOOL_UNDEFINED)
            .checkField("type", CrashAndroid.Crash.NATIVE)
            .checkField("virtualMachine", "JVM".toByteArray())
            .checkField("virtualMachineVersion", "".toByteArray())
            .checkField("pluginEnvironment", arrayOfNulls<CrashAndroid.BytesPair>(0))
            .checkAll()
        ObjectPropertyAssertions(crash.native_)
            .withFinalFieldOnly(false)
            .withIgnoredFields("handler")
            .checkField("nativeCrashPayload", nativeCrashValue)
            .checkAll()
        ObjectPropertyAssertions(crash.native_.handler)
            .withFinalFieldOnly(false)
            .checkField("source", CrashAndroid.CRASHPAD)
            .checkField("version", version)
            .checkAll()
    }

    @Test
    fun allNulls() {
        val crash = converter.fromModel(
            NativeCrashModel(
                byteArrayOf(), NativeCrashHandlerDescription(NativeCrashSource.UNKNOWN, null)
            )
        )

        //check for nulls
        MessageNano.toByteArray(crash)
        ObjectPropertyAssertions(crash)
            .withFinalFieldOnly(false)
            .withIgnoredFields("native_")
            .checkField("throwable", null as Any?)
            .checkField("threads", null as Any?)
            .checkField("methodCallStacktrace", arrayOfNulls<CrashAndroid.StackTraceElement>(0))
            .checkField("buildId", "")
            .checkField("isOffline", CrashAndroid.OPTIONAL_BOOL_UNDEFINED)
            .checkField("type", CrashAndroid.Crash.NATIVE)
            .checkField("virtualMachine", "JVM".toByteArray())
            .checkField("virtualMachineVersion", "".toByteArray())
            .checkField("pluginEnvironment", arrayOfNulls<CrashAndroid.BytesPair>(0))
            .checkAll()
        ObjectPropertyAssertions(crash.native_)
            .withFinalFieldOnly(false)
            .withIgnoredFields("handler")
            .checkField("nativeCrashPayload", byteArrayOf())
            .checkAll()
        ObjectPropertyAssertions(crash.native_.handler)
            .withFinalFieldOnly(false)
            .checkField("source", CrashAndroid.UNKNOWN)
            .checkField("version", "")
            .checkAll()
    }

    @Test(expected = UnsupportedOperationException::class)
    fun doesNotConvertToModel() {
        converter.toModel(CrashAndroid.Crash())
    }
}
