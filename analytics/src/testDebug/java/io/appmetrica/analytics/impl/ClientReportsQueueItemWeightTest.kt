package io.appmetrica.analytics.impl

import android.os.Bundle
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ClientReportsQueueItemWeightTest : CommonTest() {

    @Test
    fun ofIsFixedOverheadPlusValueBytesSize() {
        val payloadBytes = ByteArray(100) { 1 }
        val event = mock<TrimmedCoreClientEvent>()
        whenever(event.valueBytes).thenReturn(payloadBytes)
        val report = mock<ReportToSend>()
        whenever(report.report).thenReturn(event)

        assertThat(ClientReportsQueueItemWeight.of(report))
            .isEqualTo(100L + Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES)
    }

    @Test
    fun ofWithNullValueBytesIsFixedOverheadOnly() {
        val event = mock<TrimmedCoreClientEvent>()
        whenever(event.valueBytes).thenReturn(null)
        val report = mock<ReportToSend>()
        whenever(report.report).thenReturn(event)

        assertThat(ClientReportsQueueItemWeight.of(report))
            .isEqualTo(Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES)
    }

    @Test
    fun ofTypedReportIsFixedOverheadOnly() {
        val bundle = mock<Bundle>()
        assertThat(ClientReportsQueueItemWeight.ofTypedReport(bundle))
            .isEqualTo(Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES)
    }

    @Test
    fun sessionCommandWeightIsFixedOverhead() {
        assertThat(ClientReportsQueueItemWeight.sessionCommand())
            .isEqualTo(Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES)
    }
}
