package io.appmetrica.analytics.impl.permissions

import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.modulesapi.internal.common.AskForPermissionStrategyModuleProvider

class DefaultAskForPermissionStrategyProvider : AskForPermissionStrategyModuleProvider {

    override val askForPermissionStrategy: PermissionStrategy = NeverForbidPermissionStrategy()
}
