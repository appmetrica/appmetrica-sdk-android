package io.appmetrica.analytics.coreutils.internal.permission

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy

class AlwaysAllowPermissionStrategy : PermissionResolutionStrategy {

    override fun hasNecessaryPermissions(context: Context): Boolean = true
}
