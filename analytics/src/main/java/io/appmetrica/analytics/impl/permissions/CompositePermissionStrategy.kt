package io.appmetrica.analytics.impl.permissions

import io.appmetrica.analytics.coreapi.internal.permission.PermissionStrategy
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

class CompositePermissionStrategy(
    private vararg val strategies: PermissionStrategy
) : PermissionStrategy {

    private val tag = "[CompositePermissionStrategy]"

    override fun forbidUsePermission(permission: String): Boolean = strategies.any {
        it.forbidUsePermission(permission).also { value ->
            YLogger.info(tag, "$it forbidUsePermission? $value")
        }
    }

    override fun toString(): String {
        return "CompositePermissionStrategy(strategies=${strategies.contentToString()})"
    }
}
