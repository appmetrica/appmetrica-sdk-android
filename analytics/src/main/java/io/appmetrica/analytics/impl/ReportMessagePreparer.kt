package io.appmetrica.analytics.impl

import io.appmetrica.analytics.impl.request.DbNetworkTaskConfig
import io.appmetrica.analytics.impl.request.ReportRequestConfig
import io.appmetrica.analytics.impl.telephony.TelephonyDataProvider
import io.appmetrica.analytics.impl.utils.limitation.Trimmer

internal class ReportMessagePreparer(
    private val dbInteractor: ReportTaskDbInteractor,
    trimmer: Trimmer<ByteArray>,
    selfReporter: IReporterExtended,
    telephonyDataProvider: TelephonyDataProvider,
) {

    private val collector = ReportSessionsCollector(dbInteractor, trimmer, selfReporter)
    private val builder = ReportMessageBuilder(telephonyDataProvider)

    fun prepare(
        queryValues: Map<String, String>,
        requestConfig: ReportRequestConfig,
        certificates: List<String>,
        dbRequestConfig: DbNetworkTaskConfig,
    ): PreparedReport? {
        val sessionData = collector.collect(queryValues, requestConfig)
        if (sessionData.sessions.isEmpty()) return null
        val message = builder.build(sessionData, dbRequestConfig, requestConfig, certificates)
        val requestId = dbInteractor.getNextRequestId()
        return PreparedReport(message, sessionData.internalSessionsIds, requestId)
    }
}
