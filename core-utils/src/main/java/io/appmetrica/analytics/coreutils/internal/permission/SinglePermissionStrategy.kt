package io.appmetrica.analytics.coreutils.internal.permission

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor

class SinglePermissionStrategy(
    private val permissionExtractor: PermissionExtractor,
    private val permission: String
) : PermissionResolutionStrategy {

    override fun hasNecessaryPermissions(context: Context): Boolean =
        permissionExtractor.hasPermission(context, permission)
}
