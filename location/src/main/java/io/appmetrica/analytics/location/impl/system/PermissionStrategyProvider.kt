package io.appmetrica.analytics.location.impl.system

import io.appmetrica.analytics.coreapi.internal.permission.PermissionResolutionStrategy
import io.appmetrica.analytics.coreapi.internal.system.PermissionExtractor

internal interface PermissionStrategyProvider {

    fun getPermissionResolutionStrategy(permissionExtractor: PermissionExtractor): PermissionResolutionStrategy
}
