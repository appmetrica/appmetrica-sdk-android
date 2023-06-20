package io.appmetrica.analytics.coreutils.internal.permission

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor

class AnyOfPermissionStrategy(
    permissionExtractor: PermissionExtractor,
    vararg permissions: String
) : MultiplePermissionBaseStrategy(permissionExtractor, permissions.toList()) {

    override fun hasNecessaryPermissions(
        context: Context,
        permissionExtractor: PermissionExtractor,
        permissions: List<String>
    ): Boolean = permissions.any { permissionExtractor.hasPermission(context, it) }
}
