package io.appmetrica.analytics.impl.component.processor.event

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.InternalEvents
import io.appmetrica.analytics.impl.component.ComponentUnit
import io.appmetrica.analytics.impl.db.VitalComponentDataProvider
import io.appmetrica.analytics.impl.referrer.common.ReferrerInfo
import io.appmetrica.analytics.impl.referrer.service.ReferrerListener
import io.appmetrica.analytics.impl.referrer.service.ReferrerManager
import io.appmetrica.analytics.impl.referrer.service.ReferrerResult
import io.appmetrica.analytics.testutils.CommonTest
import io.appmetrica.analytics.testutils.GlobalServiceLocatorRule
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class SendReferrerEventHandlerTest : CommonTest() {
    @get:Rule
    val globalServiceLocatorRule: GlobalServiceLocatorRule = GlobalServiceLocatorRule()

    private val vitalComponentDataProvider: VitalComponentDataProvider = mock()
    private val mComponentUnit: ComponentUnit = mock {
        on { vitalComponentDataProvider } doReturn vitalComponentDataProvider
    }
    private val report: CounterReport = mock()

    private val referrerManager: ReferrerManager by setUp { GlobalServiceLocator.getInstance().getReferrerManager() }

    private val sendReferrerEventHandler by setUp { SendReferrerEventHandler(mComponentUnit) }

    @Test
    fun `process returns false`() {
        assertThat(sendReferrerEventHandler.process(report)).isFalse()
    }

    @Test
    fun `process does not request referrer when already handled`() {
        whenever(vitalComponentDataProvider.referrerHandled).doReturn(true)
        sendReferrerEventHandler.process(report)
        verify(referrerManager, never()).requestReferrer(any())
    }

    @Test
    fun `process requests referrer when not handled yet`() {
        whenever(vitalComponentDataProvider.referrerHandled).doReturn(false)
        sendReferrerEventHandler.process(report)
        verify(referrerManager).requestReferrer(any())
    }

    private fun sendReferrer(result: ReferrerResult) {
        val listenerCaptor = argumentCaptor<ReferrerListener>()
        verify(referrerManager).requestReferrer(listenerCaptor.capture())
        listenerCaptor.firstValue.onResult(result)
    }

    @Test
    fun `onResult does not handle report when referrerInfo is null`() {
        whenever(vitalComponentDataProvider.referrerHandled).doReturn(false)

        sendReferrerEventHandler.process(report)

        val result: ReferrerResult = mock {
            on { this.referrerInfo } doReturn null
        }
        sendReferrer(result)

        verify(mComponentUnit, never()).handleReport(any())
        verify(vitalComponentDataProvider, never()).referrerHandled = true
    }

    @Test
    fun `onResult does not handle report when referrer already sent`() {
        whenever(vitalComponentDataProvider.referrerHandled).doReturn(false)

        sendReferrerEventHandler.process(report)

        whenever(vitalComponentDataProvider.referrerHandled).doReturn(true)

        val result: ReferrerResult = mock {
            on { this.referrerInfo } doReturn mock()
        }
        sendReferrer(result)

        verify(mComponentUnit, never()).handleReport(any())
        verify(vitalComponentDataProvider, never()).referrerHandled = true
    }

    @Test
    fun `onResult handles report and marks referrer as handled`() {
        whenever(vitalComponentDataProvider.referrerHandled).doReturn(false)

        sendReferrerEventHandler.process(report)

        val referrerInfo: ReferrerInfo = mock()
        val result: ReferrerResult = mock {
            on { this.referrerInfo } doReturn referrerInfo
        }
        sendReferrer(result)

        val reportCaptor = argumentCaptor<CounterReport>()
        verify(mComponentUnit).handleReport(reportCaptor.capture())

        val report = reportCaptor.firstValue
        assertThat(report.type).isEqualTo(InternalEvents.EVENT_TYPE_SEND_REFERRER.typeId)
        assertThat(report.valueBytes).isEqualTo(referrerInfo.toProto())

        verify(vitalComponentDataProvider).referrerHandled = true
    }

    @Test
    fun `onResult does not mark referrer as handled when handleReport throws exception`() {
        whenever(vitalComponentDataProvider.referrerHandled).doReturn(false)
        doThrow(RuntimeException("Test exception")).whenever(mComponentUnit).handleReport(any())

        sendReferrerEventHandler.process(report)

        val result: ReferrerResult = mock {
            on { this.referrerInfo } doReturn mock()
        }
        sendReferrer(result)

        verify(vitalComponentDataProvider, never()).referrerHandled = true
    }
}
