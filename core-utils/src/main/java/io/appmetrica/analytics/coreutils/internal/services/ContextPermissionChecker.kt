package io.appmetrica.analytics.coreutils.internal.services

import android.content.Context
import android.content.pm.PackageManager

object ContextPermissionChecker {
    @JvmStatic
    fun hasPermission(context: Context, permission: String): Boolean = try {
        PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(permission)
    } catch (ignored: Throwable) {
        false
    }
}
