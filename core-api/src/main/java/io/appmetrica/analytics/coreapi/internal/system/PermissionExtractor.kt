package io.appmetrica.analytics.coreapi.internal.system

import android.content.Context

interface PermissionExtractor {

    fun hasPermission(context: Context, permission: String): Boolean
}
