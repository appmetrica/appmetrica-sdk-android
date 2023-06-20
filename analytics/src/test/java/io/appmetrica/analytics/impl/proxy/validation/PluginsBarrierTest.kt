package io.appmetrica.analytics.impl.proxy.validation

import io.appmetrica.analytics.ValidationException
import io.appmetrica.analytics.plugins.PluginErrorDetails
import io.appmetrica.analytics.testutils.CommonTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PluginsBarrierTest : CommonTest() {

    @Mock
    private lateinit var errorDetails: PluginErrorDetails
    private val barrier = PluginsBarrier()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun reportPluginUnhandledExceptionNonNull() {
        barrier.reportUnhandledException(errorDetails)
    }

    @Test(expected = ValidationException::class)
    fun reportPluginUnhandledExceptionNull() {
        barrier.reportUnhandledException(null)
    }

    @Test
    fun reportErrorNonNull() {
        val message = "some message"
        barrier.reportError(errorDetails, message)
    }

    fun reportErrorNonNullMessageIsNull() {
        barrier.reportError(errorDetails, null)
    }

    fun reportErrorNonNullMessageIsEmpty() {
        barrier.reportError(errorDetails, "")
    }

    @Test(expected = ValidationException::class)
    fun reportPluginReportErrorNull() {
        val message = "some message"
        barrier.reportError(null, message)
    }

    @Test
    fun reportErrorWithStacktraceFilled() {
        `when`(errorDetails.stacktrace).thenReturn(listOf(mock()))
        val message = "some message"
        assertTrue(barrier.reportErrorWithFilledStacktrace(errorDetails, message))
    }

    @Test
    fun reportErrorWithStacktraceEmpty() {
        `when`(errorDetails.stacktrace).thenReturn(emptyList())
        val message = "some message"
        assertFalse(barrier.reportErrorWithFilledStacktrace(errorDetails, message))
    }

    fun reportErrorWithStacktraceNonNullMessageIsNull() {
        assertTrue(barrier.reportErrorWithFilledStacktrace(errorDetails, null))
    }

    fun reportErrorWithStacktraceNonNullMessageIsEmpty() {
        assertTrue(barrier.reportErrorWithFilledStacktrace(errorDetails, ""))
    }

    @Test(expected = ValidationException::class)
    fun reportPluginErrorWithStacktraceNull() {
        val message = "some message"
        barrier.reportErrorWithFilledStacktrace(null, message)
    }

    @Test
    fun reportErrorWithIdentifierFilled() {
        barrier.reportError("identifier", "message", errorDetails)
    }

    @Test
    fun reportErrorWithIdentifierNullMessage() {
        barrier.reportError("identifier", null, errorDetails)
    }

    @Test(expected = ValidationException::class)
    fun reportErrorWithIdentifierNullId() {
        barrier.reportError(null, "message", errorDetails)
    }

    @Test(expected = ValidationException::class)
    fun reportErrorWithIdentifierEmptyId() {
        barrier.reportError("", "message", errorDetails)
    }

    @Test
    fun reportErrorWithIdentifierNullError() {
        barrier.reportError("identifier", "message", null)
    }
}
