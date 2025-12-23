package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.idsync.internal.model.RequestConfig
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

internal class IdSyncResultHandler(
    serviceContext: ServiceContext
) {
    private val tag = "[IdSyncResultHandler]"
    private val eventValueComposer = IdSyncResultStringComposer()
    private val resultReportersProvider = IdSyncResultReporterProvider(serviceContext)

    fun reportEvent(result: RequestResult, requestConfig: RequestConfig, sdkIdentifiers: SdkIdentifiers) {
        val value = eventValueComposer.compose(result)
        DebugLogger.info(
            tag,
            "Handle result: $result and report value: $value. RequestConfig: " +
                "$requestConfig; sdkIdentifiers: $sdkIdentifiers"
        )
        resultReportersProvider.getReporters(requestConfig).forEach {
            it.reportResult(value, sdkIdentifiers)
        }
    }
}
