package io.appmetrica.analytics.remotepermissions.impl

import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class RemoteConfigPermissionStrategy :
    PermissionStrategy {
    private val tag = "[RemoteConfigPermissionStrategy]"

    private var permittedPermissions: Set<String> = emptySet()

    @Synchronized
    fun updatePermissions(permittedPermissions: Set<String>) {
        DebugLogger.info(tag, "update permissions set: $permittedPermissions")
        this.permittedPermissions = permittedPermissions
    }

    @Synchronized
    override fun forbidUsePermission(permission: String): Boolean {
        return (!permittedPermissions.contains(permission)).also {
            DebugLogger.info(tag, "forbidUsePermission `$permission`: $it")
        }
    }
}
