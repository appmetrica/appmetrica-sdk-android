package io.appmetrica.analytics.impl.modules.plugin

internal fun interface PluginDetectionStrategy {

    fun isPresent(): Boolean
}
