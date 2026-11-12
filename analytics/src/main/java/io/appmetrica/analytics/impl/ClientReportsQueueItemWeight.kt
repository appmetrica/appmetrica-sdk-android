package io.appmetrica.analytics.impl

import android.os.Bundle

/**
 * Rough retained-size estimate for items held on the CRS queue.
 * Not a precise heap profiler — used only for the byte budget gate.
 *
 * Event reports: fixed overhead C plus [TrimmedCoreClientEvent.valueBytes] size.
 * Typed reports / session commands: C only (typed Bundle payload is intentionally
 * not walked — accepted underestimation until a new agreement).
 */
internal object ClientReportsQueueItemWeight {

    fun of(report: ReportToSend): Long {
        val valueBytesSize = report.report.valueBytes?.size?.toLong() ?: 0L
        return valueBytesSize + Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES
    }

    @Suppress("UNUSED_PARAMETER")
    fun ofTypedReport(bundle: Bundle): Long =
        Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES

    fun sessionCommand(): Long = Limits.CLIENT_REPORTS_QUEUE_ITEM_FIXED_OVERHEAD_BYTES
}
