package io.appmetrica.analytics.impl.proxy.validation

import io.appmetrica.analytics.ValidationException
import io.appmetrica.analytics.plugins.PluginErrorDetails
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class PluginsReporterBarrierTest : CommonTest() {

    private val errorDetails: PluginErrorDetails = mock()
    private val barrier = PluginsReporterBarrier()

    @Test
    fun reportUnhandledException() {
        barrier.reportUnhandledException(errorDetails)
    }

    @Test(expected = ValidationException::class)
    fun reportUnhandledExceptionIfUnhandledExceptionIsNull() {
        barrier.reportUnhandledException(null)
    }

    @Test
    fun reportError() {
        val message = "some message"
        barrier.reportError(errorDetails, message)
    }

    @Test
    fun reportErrorIfMessageIsNull() {
        barrier.reportError(errorDetails, null)
    }

    @Test
    fun reportErrorIfMessageIsEmpty() {
        barrier.reportError(errorDetails, "")
    }

    @Test(expected = ValidationException::class)
    fun reportErrorIfErrorDetailsIsNull() {
        val message = "some message"
        barrier.reportError(null, message)
    }

    @Test
    fun reportErrorWithFilledStacktrace() {
        whenever(errorDetails.stacktrace).thenReturn(listOf(mock()))
        val message = "some message"
        Assertions.assertThat(barrier.reportErrorWithFilledStacktrace(errorDetails, message)).isTrue()
    }

    @Test
    fun reportErrorWithFilledStacktraceIfStacktraceIsEmpty() {
        whenever(errorDetails.stacktrace).thenReturn(emptyList())
        val message = "some message"
        Assertions.assertThat(barrier.reportErrorWithFilledStacktrace(errorDetails, message)).isFalse()
    }

    @Test
    fun reportErrorWithFilledStacktraceIfMessageIsNull() {
        whenever(errorDetails.stacktrace).thenReturn(listOf(mock()))
        Assertions.assertThat(barrier.reportErrorWithFilledStacktrace(errorDetails, null)).isTrue()
    }

    @Test
    fun reportErrorWithFilledStacktraceIfMessageIsEmpty() {
        whenever(errorDetails.stacktrace).thenReturn(listOf(mock()))
        val message = "some message"
        Assertions.assertThat(barrier.reportErrorWithFilledStacktrace(errorDetails, "")).isTrue()
    }

    @Test(expected = ValidationException::class)
    fun reportErrorWithFilledStacktraceIfErrorDetailsIsNull() {
        val message = "some message"
        barrier.reportErrorWithFilledStacktrace(null, message)
    }

    @Test
    fun reportErrorWithErrorDetails() {
        barrier.reportError("identifier", "message", errorDetails)
    }

    @Test
    fun reportErrorWithErrorDetailsIfMessageIsNull() {
        barrier.reportError("identifier", null, errorDetails)
    }

    @Test(expected = ValidationException::class)
    fun reportErrorWithErrorDetailsIdIdentifierIsNull() {
        barrier.reportError(null, "message", errorDetails)
    }

    @Test(expected = ValidationException::class)
    fun reportErrorWithErrorDetailsIfIdentifierIsEmpty() {
        barrier.reportError("", "message", errorDetails)
    }

    @Test
    fun reportErrorIsErrorDetailsIsNull() {
        barrier.reportError("identifier", "message", null)
    }
}
