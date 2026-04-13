package io.appmetrica.analytics.impl.crash.jvm

import io.appmetrica.analytics.impl.IOUtils
import io.appmetrica.gradle.testutils.CommonTest
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule.Companion.constructionRule
import io.appmetrica.gradle.testutils.rules.MockedStaticRule.Companion.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

internal class JvmCrashReaderTest : CommonTest() {

    private val fileModifiedTimestamp = 1700000000000L

    private val crashFile: File = mock {
        on { lastModified() } doReturn fileModifiedTimestamp
    }

    @get:Rule
    val ioUtilsRule = staticRule<IOUtils>()

    @get:Rule
    val jvmCrashRule = constructionRule<JvmCrash>()

    private val crashReader: JvmCrashReader by setUp { JvmCrashReader() }

    @Test
    fun handleCrashDataReturnsNullForBlankData() {
        assertThat(crashReader.handleCrashData(crashFile, null, fileModifiedTimestamp)).isNull()
        assertThat(crashReader.handleCrashData(crashFile, "", fileModifiedTimestamp)).isNull()
    }

    @Test
    fun handleCrashDataCreatesAndReturnsJvmCrashWithCorrectArgs() {
        val crashData = "some crash json"
        val result = crashReader.handleCrashData(crashFile, crashData, fileModifiedTimestamp)
        assertThat(jvmCrashRule.constructionMock.constructed()).hasSize(1)
        assertThat(result).isSameAs(jvmCrashRule.constructionMock.constructed()[0])
        assertThat(jvmCrashRule.argumentInterceptor.flatArguments())
            .containsExactly(crashData, fileModifiedTimestamp)
    }

    @Test
    fun applyReturnsNullWhenIOUtilsReturnsNull() {
        whenever(IOUtils.getStringFileLocked(crashFile)).thenReturn(null)
        assertThat(crashReader.apply(crashFile)).isNull()
    }

    @Test
    fun applyCreatesAndReturnsJvmCrashForValidData() {
        val crashData = "some crash json"
        whenever(IOUtils.getStringFileLocked(crashFile)).thenReturn(crashData)
        val result = crashReader.apply(crashFile)

        assertThat(jvmCrashRule.constructionMock.constructed()).hasSize(1)
        assertThat(result).isSameAs(jvmCrashRule.constructionMock.constructed()[0])
        assertThat(jvmCrashRule.argumentInterceptor.flatArguments())
            .containsExactly(crashData, fileModifiedTimestamp)
    }

    @Test
    fun consumeDeletesCrashFile() {
        whenever(crashFile.delete()).thenReturn(true)
        crashReader.consume(crashFile)
        verify(crashFile).delete()
    }

    @Test
    fun consumeHandlesFalseDeleteWithoutThrowing() {
        whenever(crashFile.delete()).thenReturn(false)
        crashReader.consume(crashFile)
        verify(crashFile).delete()
    }

    @Test
    fun consumeHandlesExceptionFromDeleteWithoutThrowing() {
        whenever(crashFile.delete()).thenThrow(SecurityException("no permission"))
        crashReader.consume(crashFile)
        verify(crashFile).delete()
    }
}
