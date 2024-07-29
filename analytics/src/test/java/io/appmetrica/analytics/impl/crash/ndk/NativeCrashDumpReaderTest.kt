package io.appmetrica.analytics.impl.crash.ndk

import io.appmetrica.analytics.coreutils.internal.io.Base64Utils
import io.appmetrica.analytics.impl.IOUtils
import io.appmetrica.analytics.impl.crash.jvm.converter.NativeCrashConverter
import io.appmetrica.analytics.impl.protobuf.backend.CrashAndroid.Crash
import io.appmetrica.analytics.protobuf.nano.MessageNano
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class NativeCrashDumpReaderTest : CommonTest() {
    @get:Rule
    val sIoUtilsMockedRule = staticRule<IOUtils>()

    @get:Rule
    val sBase64UtilsMockedRule = staticRule<Base64Utils>()

    private val description = mock<NativeCrashHandlerDescription>()
    private val converter = mock<NativeCrashConverter>()

    private val nativeCrashDumpReader by setUp { NativeCrashDumpReader(description, converter) }

    @Test
    fun readDump() {
        val (absPath, file) = mockFile()
        val crash = Crash().apply {
            buildId = "buildid"
        }
        val fileData = MessageNano.toByteArray(crash)
        val fileDataString = "filedata"

        whenever(IOUtils.readAll(absPath)).thenReturn(fileData)
        whenever(converter.fromModel(argThat { data.contentEquals(fileData) && handlerDescription == description }))
            .thenReturn(crash)
        whenever(Base64Utils.compressBase64(fileData)).thenReturn(fileDataString)

        assertThat(nativeCrashDumpReader.apply(file)).isEqualTo(fileDataString)
    }

    @Test
    fun emptyDump() {
        val (absPath, file) = mockFile()
        val fileData = byteArrayOf()

        whenever(IOUtils.readAll(absPath)).thenReturn(fileData)

        assertThat(nativeCrashDumpReader.apply(file)).isNull()
    }

    @Test
    fun nullDump() {
        val (absPath, file) = mockFile()
        whenever(IOUtils.readAll(absPath)).thenReturn(null)
        assertThat(nativeCrashDumpReader.apply(file)).isNull()
    }

    @Test
    fun exceptionWhenRead() {
        val (_, file) = mockFile()
        whenever(Base64Utils.compressBase64(any())).thenThrow(RuntimeException())
        assertThat(nativeCrashDumpReader.apply(file)).isNull()
    }

    private fun mockFile(absPath: String = "fileabspath") =
        absPath to mock<File> { on { absolutePath } doReturn absPath }
}
