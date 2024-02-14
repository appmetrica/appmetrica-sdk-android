package io.appmetrica.analytics.impl.crash.ndk

import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.impl.ReportConsumer
import io.appmetrica.analytics.ndkcrashesapi.internal.NativeCrash
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.staticRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.refEq
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class NativeCrashReporterTest : CommonTest() {
    @get:Rule
    internal val appMetricaNativeCrashConverterMockedRule = staticRule<AppMetricaNativeCrashConverter>()

    private val reportConsumer = mock<ReportConsumer>()
    private val markCrashCompletedFunc = mock<(String) -> Unit>()

    private val reporter by setUp { NativeCrashReporter(reportConsumer, markCrashCompletedFunc) }

    @Test
    fun newCrash() {
        val (uuid, crash, appMetricaCrash) = createAppMetricaCrash()
        reporter.newCrash(crash)

        val finalizerFunCaptor = argumentCaptor<Consumer<File>>()
        verify(reportConsumer).consumeCurrentSessionNativeCrash(refEq(appMetricaCrash), finalizerFunCaptor.capture())
        verifyNoMoreInteractions(reportConsumer)

        // check call finalizer
        finalizerFunCaptor.firstValue.consume(mock())
        verify(markCrashCompletedFunc).invoke(eq(uuid))
    }

    @Test
    fun `newCrash broken crash`() {
        val (uuid, crash) = createCrash()
        reporter.newCrash(crash)

        verify(reportConsumer, never()).consumeCurrentSessionNativeCrash(any(), any())
        verify(markCrashCompletedFunc).invoke(eq(uuid))
    }

    @Test
    fun reportCrashesFromPrevSession() {
        val (firstUuid, firstCrash, firstAppMetricaCrash) = createAppMetricaCrash()
        val (secondUuid, secondCrash, secondAppMetricaCrash) = createAppMetricaCrash()
        val (thirdUuid, thirdCrash) = createCrash()

        reporter.reportCrashesFromPrevSession(listOf(firstCrash, secondCrash, thirdCrash))

        val finalizerFunCaptor = argumentCaptor<Consumer<File>>()

        // first crash
        verify(reportConsumer).consumePrevSessionNativeCrash(eq(firstAppMetricaCrash), finalizerFunCaptor.capture())
        finalizerFunCaptor.lastValue.consume(mock())
        verify(markCrashCompletedFunc).invoke(eq(firstUuid))

        // second crash
        verify(reportConsumer).consumePrevSessionNativeCrash(eq(secondAppMetricaCrash), finalizerFunCaptor.capture())
        finalizerFunCaptor.lastValue.consume(mock())
        verify(markCrashCompletedFunc).invoke(eq(secondUuid))

        // third crash
        verifyNoMoreInteractions(reportConsumer)
        verify(markCrashCompletedFunc).invoke(eq(thirdUuid))
    }

    @Test
    fun `reportCrashesFromPrevSession zero crashes`() {
        reporter.reportCrashesFromPrevSession(emptyList())

        verifyNoMoreInteractions(reportConsumer)
        verifyNoMoreInteractions(markCrashCompletedFunc)
    }

    private fun createCrash(): Pair<String, NativeCrash> {
        val uuid = UUID.randomUUID().toString()
        val crash = mock<NativeCrash> {
            on { this.uuid } doReturn uuid
        }
        return uuid to crash
    }

    private fun createAppMetricaCrash(): Triple<String, NativeCrash, AppMetricaNativeCrash> {
        val (uuid, crash) = createCrash()
        val appMetricaCrash = mock<AppMetricaNativeCrash>()
        whenever(AppMetricaNativeCrashConverter.from(crash)).thenReturn(appMetricaCrash)
        return Triple(uuid, crash, appMetricaCrash)
    }
}
