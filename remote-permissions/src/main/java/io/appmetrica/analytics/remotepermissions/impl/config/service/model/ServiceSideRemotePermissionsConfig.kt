package io.appmetrica.analytics.remotepermissions.impl.config.service.model

import io.appmetrica.analytics.remotepermissions.impl.Constants

internal class ServiceSideRemotePermissionsConfig(
    val permittedPermissions: Set<String> = Constants.Defaults.DEFAULT_PERMISSIONS
) {
    override fun toString(): String {
        return "ServiceSideRemotePermissionsConfig(permittedPermissions=$permittedPermissions)"
    }
}
