package io.appmetrica.analytics.modulesapi.internal.common

import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy

interface AskForPermissionStrategyModuleProvider {

    val askForPermissionStrategy: PermissionStrategy
}
