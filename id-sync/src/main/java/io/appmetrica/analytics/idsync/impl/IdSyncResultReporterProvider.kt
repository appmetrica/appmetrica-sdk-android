package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

class IdSyncResultReporterProvider(private val serviceContext: ServiceContext) {

    private val tag = "[IdSyncResultReporterProvider]"

    fun getReporters(requestConfig: RequestConfig): List<IdSyncResultReporter> {
        val reporters = mutableListOf<IdSyncResultReporter>()
        if (requestConfig.reportEventEnabled) {
            reporters.add(IdSyncResultEventReporter(serviceContext))
            DebugLogger.info(tag, "Included event reporter")
        }
        if (!requestConfig.reportUrl.isNullOrBlank()) {
            reporters.add(IdSyncResultRealtimeReporter(serviceContext, requestConfig.reportUrl))
            DebugLogger.info(tag, "Included realtime reporter")
        }
        return reporters
    }
}
