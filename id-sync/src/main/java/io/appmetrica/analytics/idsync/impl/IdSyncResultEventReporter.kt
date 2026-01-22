package io.appmetrica.analytics.idsync.impl

import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

internal class IdSyncResultEventReporter(private val serviceContext: ServiceContext) : IdSyncResultReporter {
    private val eventName = "id_sync"

    override fun reportResult(value: String, sdkIdentifiers: SdkIdentifiers) {
        serviceContext.selfReporter.reportEvent(eventName, value)
    }
}
