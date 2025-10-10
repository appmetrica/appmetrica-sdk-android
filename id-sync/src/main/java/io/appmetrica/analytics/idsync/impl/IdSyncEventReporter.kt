package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

internal class IdSyncEventReporter(
    private val serviceContext: ServiceContext
) {
    private val tag = "[IdSyncEventReporter]"

    private val eventValueComposer = IdSyncEventValueComposer()
    private val eventName = "id_sync"

    fun reportEvent(result: RequestResult) {
        DebugLogger.info(tag, "Reporting event with name: $eventName and value: ${eventValueComposer.compose(result)}")
        serviceContext.selfReporter.reportEvent(eventName, eventValueComposer.compose(result))
    }
}
