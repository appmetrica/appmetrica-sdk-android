package io.appmetrica.analytics.impl.referrer.service.provider

import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.impl.selfreporting.AppMetricaSelfReportFacade
import io.appmetrica.analytics.impl.selfreporting.SelfReporterWrapper
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.on
import io.appmetrica.analytics.testutils.staticRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

internal class SafeReferrerProviderTest : CommonTest() {
    private val delegate: ReferrerProvider = mock()

    private val selfReporter: SelfReporterWrapper = mock()

    @get:Rule
    val appMetricaSelfReportFacadeRule = staticRule<AppMetricaSelfReportFacade> {
        on { AppMetricaSelfReportFacade.getReporter() } doReturn selfReporter
    }

    private val safeReferrerProvider by setUp { SafeReferrerProvider(delegate) }

    @Test
    fun `referrerName delegates to delegate provider`() {
        whenever(delegate.referrerName).thenReturn("test-referrer")

        assertThat(safeReferrerProvider.referrerName).isEqualTo("test-referrer")
    }

    @Test
    fun `requestReferrer calls delegate`() {
        val listener: ReferrerListener = mock()

        safeReferrerProvider.requestReferrer(listener)

        verify(delegate).requestReferrer(listener)
        verifyNoInteractions(selfReporter)
    }

    @Test
    fun `requestReferrer catches exception from delegate and notifies listener with Failure`() {
        val listener: ReferrerListener = mock()
        val exception = RuntimeException("Test exception")
        whenever(delegate.referrerName).thenReturn("test-referrer")
        whenever(delegate.requestReferrer(any())).doThrow(exception)

        safeReferrerProvider.requestReferrer(listener)

        val resultCaptor = argumentCaptor<ReferrerResult>()
        verify(listener).onResult(resultCaptor.capture())

        assertThat(resultCaptor.firstValue).isInstanceOf(ReferrerResult.Failure::class.java)
        val failure = resultCaptor.firstValue as ReferrerResult.Failure
        assertThat(failure.message).isEqualTo("Failed to request test-referrer referrer")
        assertThat(failure.throwable).isEqualTo(exception)

        verify(selfReporter).reportError(any(), any<Throwable>())
    }
}
