package io.appmetrica.analytics.coreutils.internal.permission

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor

abstract class MultiplePermissionBaseStrategy(
    private val permissionExtractor: PermissionExtractor,
    private val permissions: List<String>
) : PermissionResolutionStrategy {

    override fun hasNecessaryPermissions(context: Context): Boolean =
        permissions.isEmpty() || hasNecessaryPermissions(context, permissionExtractor, permissions)

    abstract fun hasNecessaryPermissions(
        context: Context,
        permissionExtractor: PermissionExtractor,
        permissions: List<String>
    ): Boolean
}
