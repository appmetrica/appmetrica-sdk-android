package io.appmetrica.analytics.impl.permissions

import android.content.Context
import io.appmetrica.analytics.coreapi.internal.permission.PermissionState
import io.appmetrica.analytics.coreutils.internal.collection.CollectionUtils.areCollectionsEqual

internal class PermissionsChecker {

    fun check(context: Context, fromDb: List<PermissionState>?): List<PermissionState>? {
        val fromSystem = RuntimePermissionsRetriever(context).permissionsState
        return if (areCollectionsEqual(fromSystem, fromDb)) {
            null
        } else {
            fromSystem
        }
    }
}
