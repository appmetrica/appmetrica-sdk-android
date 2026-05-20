package io.appmetrica.analytics.remotepermissions.internal

import io.appmetrica.analytics.remotepermissions.impl.config.service.model.ServiceSideRemotePermissionsConfig

class ServiceSideRemotePermissionsConfigWrapper internal constructor(
    internal val config: ServiceSideRemotePermissionsConfig
) {

    companion object {
        internal fun ServiceSideRemotePermissionsConfig.toWrapper() =
            ServiceSideRemotePermissionsConfigWrapper(this)
    }
}
