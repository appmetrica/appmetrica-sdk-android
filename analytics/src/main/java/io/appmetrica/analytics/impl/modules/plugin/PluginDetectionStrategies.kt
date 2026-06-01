package io.appmetrica.analytics.impl.modules.plugin

import io.appmetrica.analytics.coreutils.internal.reflection.ReflectionUtils

internal object PluginDetectionStrategies {

    fun byClass(vararg classNames: String): PluginDetectionStrategy =
        PluginDetectionStrategy {
            classNames.any { ReflectionUtils.detectClassExists(it) }
        }
}
