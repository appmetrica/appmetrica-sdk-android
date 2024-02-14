package io.appmetrica.analytics.remotepermissions.impl

import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.logger.internal.YLogger

class RemoteConfigPermissionStrategy :
    PermissionStrategy {
    private val tag = "[RemoteConfigPermissionStrategy]"

    private var permittedPermissions: Set<String> = emptySet()

    @Synchronized
    fun updatePermissions(permittedPermissions: Set<String>) {
        YLogger.info(tag, "update permissions set: $permittedPermissions")
        this.permittedPermissions = permittedPermissions
    }

    @Synchronized
    override fun forbidUsePermission(permission: String): Boolean {
        return (!permittedPermissions.contains(permission)).also {
            YLogger.info(tag, "forbidUsePermission `$permission`: $it")
        }
    }
}
