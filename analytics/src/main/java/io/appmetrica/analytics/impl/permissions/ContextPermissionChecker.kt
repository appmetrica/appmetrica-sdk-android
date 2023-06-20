package io.appmetrica.analytics.impl.permissions

import android.content.Context
import android.content.pm.PackageManager

internal object ContextPermissionChecker {
    @JvmStatic
    fun hasPermission(context: Context, permission: String): Boolean = try {
        PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(permission)
    } catch (ignored: Throwable) {
        false
    }
}
