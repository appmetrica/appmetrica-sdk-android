package io.appmetrica.analytics.idsync.impl.precondition

import io.appmetrica.analytics.coreapi.internal.system.NetworkType
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext

internal class CellNetworkPreconditionVerifier(
    private val serviceContext: ServiceContext
) : PreconditionVerifier {
    private val tag = "[CellNetworkPreconditionVerifier]"

    override fun matchPrecondition(): Boolean {
        val networkType = serviceContext.activeNetworkTypeProvider.getNetworkType(serviceContext.context)
        val result = networkType == NetworkType.CELL
        DebugLogger.info(tag, "Network type: $networkType; matchPrecondition: $result")
        return result
    }
}
