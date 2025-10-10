package io.appmetrica.analytics.idsync.impl.precondition

import io.appmetrica.analytics.idsync.internal.model.NetworkType
import io.appmetrica.analytics.idsync.internal.model.Preconditions
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

internal class PreconditionProvider(private val serviceContext: ServiceContext) {

    fun getPrecondition(config: Preconditions): PreconditionVerifier =
        when (config.networkType) {
            NetworkType.CELL -> CellNetworkPreconditionVerifier(serviceContext)
            else -> AnyPreconditionVerifier()
        }
}
