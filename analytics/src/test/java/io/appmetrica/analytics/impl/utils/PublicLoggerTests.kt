package io.appmetrica.analytics.impl.utils

import android.content.Context
import android.util.Log
import io.appmetrica.analytics.impl.Utils
import io.appmetrica.analytics.impl.protobuf.backend.EventProto.ReportMessage
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class PublicLoggerTests : CommonTest() {

    private val logPrefix = "AppMetricaTest"
    private val throwable: Throwable = mock()
    private val throwableStacktrace = "Throwable stacktrace"

    @get:Rule
    val logRule = staticRule<Log>()

    private val context: Context = mock {
        on { packageName } doReturn logPrefix
    }

    private val apiKey = UUID.randomUUID().toString()

    private val logger: PublicLogger by setUp { PublicLogger(apiKey) }

    @Before
    fun setUp() {
        LoggerWithApiKey.reset()
        LoggerWithApiKey.init(context)
        whenever(Log.getStackTraceString(throwable)).thenReturn(throwableStacktrace)
    }

    @Test
    fun `setEnabled for true`() {
        logger.isEnabled = true
        assertThat(logger.isEnabled).isTrue()
    }

    @Test
    fun `set enabled for false`() {
        logger.isEnabled = false
        assertThat(logger.isEnabled).isFalse()
    }

    @Test
    fun `i with message if enabled`() {
        val message = "Some message"
        logger.isEnabled = true
        logger.i(message)
        verifyInfoMessage(message)
    }

    @Test
    fun `i message if disabled`() {
        logger.isEnabled = false
        logger.i("Some message")
        verifyNoLogInteractions()
    }

    @Test
    fun `i message by default`() {
        logger.i("Some message")
        verifyNoLogInteractions()
    }

    @Test
    fun `fi message if enabled`() {
        logger.isEnabled = true
        logger.fi("Message: %s,%s", "p1", "p2")
        verifyInfoMessage("Message: p1,p2")
    }

    @Test
    fun `fi message if disabled`() {
        logger.isEnabled = false
        logger.fi("Message: %s,%s", "p1", "p2")
        verifyNoLogInteractions()
    }

    @Test
    fun `fi message by default`() {
        logger.fi("Message: %s,%s", "p1", "p2")
        verifyNoLogInteractions()
    }

    @Test
    fun `w message if enabled`() {
        val message = "message"
        logger.isEnabled = true
        logger.w(message)
        verifyWarningMessage(message)
    }

    @Test
    fun `w message if disabled`() {
        logger.isEnabled = false
        logger.w("message")
        verifyNoLogInteractions()
    }

    @Test
    fun `w message by default`() {
        logger.w("message")
        verifyNoLogInteractions()
    }

    @Test
    fun `fw message if enabled`() {
        logger.isEnabled = true
        logger.fw("message: %s,%s", "p1", "p2")
        verifyWarningMessage("message: p1,p2")
    }

    @Test
    fun `fw message if disabled`() {
        logger.isEnabled = false
        logger.fw("message: %s,%s", "p1", "p2")
        verifyNoLogInteractions()
    }

    @Test
    fun `fw message by default`() {
        logger.fw("message: %s,%s", "p1", "p2")
        verifyNoLogInteractions()
    }

    @Test
    fun `e message only if enabled`() {
        val message = "message"
        logger.isEnabled = true
        logger.e(message)
        verifyErrorMessage(message)
    }

    @Test
    fun `e message if disabled`() {
        logger.isEnabled = false
        logger.e("message")
        verifyNoLogInteractions()
    }

    @Test
    fun `e message by default`() {
        logger.e("message")
        verifyNoLogInteractions()
    }

    @Test
    fun `fe message if enabled`() {
        logger.isEnabled = true
        logger.fe("message: %s,%s", "p1", "p2")
        verifyErrorMessage("message: p1,p2")
    }

    @Test
    fun `fe message if disabled`() {
        logger.isEnabled = false
        logger.fe("message: %s, %s", "p1", "p2")
        verifyNoLogInteractions()
    }

    @Test
    fun `fe message by default`() {
        logger.fe("message: %s, %s", "p1", "p2")
        verifyNoLogInteractions()
    }

    @Test
    fun `e message with throwable if enabled`() {
        val message = "message"
        logger.isEnabled = true
        logger.e(throwable, message)
        verifyErrorMessage(formatThrowableString(message))
    }

    @Test
    fun `e message with throwable if disabled`() {
        logger.isEnabled = false
        logger.e(throwable, "message")
        verifyNoLogInteractions()
    }

    @Test
    fun `e message with throwable by default`() {
        logger.e(throwable, "message")
        verifyNoLogInteractions()
    }

    @Test
    fun `fe message with throwable if enabled`() {
        logger.isEnabled = true
        logger.fe(throwable, "message: %s, %s", "p1", "p2")
        verifyErrorMessage(formatThrowableString("message: p1, p2"))
    }

    @Test
    fun `fe message with throwable if disabled`() {
        logger.isEnabled = false
        logger.fe(throwable, "message: %s, %s", "p1", "p2")
        verifyNoLogInteractions()
    }

    @Test
    fun `fe message with throwable by default`() {
        logger.fe(throwable, "message: %s, %s", "p1", "p2")
        verifyNoLogInteractions()
    }

    @Test
    fun logSessionEvents() {
        logger.isEnabled = true
        val message = "some message"
        val firstEvent = ReportMessage.Session.Event()
        firstEvent.type = ReportMessage.Session.Event.EVENT_CLIENT
        firstEvent.name = "first name"
        firstEvent.value = "first value".toByteArray()
        val thirdEvent = ReportMessage.Session.Event()
        thirdEvent.type = ReportMessage.Session.Event.EVENT_CLIENT
        thirdEvent.name = "third name"
        thirdEvent.value = "third value".toByteArray()
        val session = ReportMessage.Session()
        session.events = arrayOfNulls(3)
        session.events[0] = firstEvent
        session.events[2] = thirdEvent
        logger.logSessionEvents(session, message)
        verifyInfoMessage("$message: first name with value first value")
        verifyInfoMessage("$message: third name with value third value")
    }

    private fun verifyInfoMessage(message: String) {
        verifyLogMessage(Log.INFO, message)
    }

    private fun verifyWarningMessage(message: String) {
        verifyLogMessage(Log.WARN, message)
    }

    private fun verifyErrorMessage(message: String) {
        verifyLogMessage(Log.ERROR, message)
    }

    private fun verifyLogMessage(logLevel: Int, message: String) {
        logRule.staticMock.verify {
            Log.println(
                logLevel,
                "AppMetrica",
                prepareMessage(message)
            )
        }
    }

    private fun verifyNoLogInteractions() {
        logRule.staticMock.verifyNoInteractions()
    }

    private fun formatThrowableString(message: String) = message + "\n" + throwableStacktrace

    private fun prepareMessage(message: String): String =
        "[$logPrefix] : [${Utils.createPartialApiKey(apiKey)}] $message"
}
