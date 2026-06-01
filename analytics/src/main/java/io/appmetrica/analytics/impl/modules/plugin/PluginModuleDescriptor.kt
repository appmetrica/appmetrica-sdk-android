package io.appmetrica.analytics.impl.modules.plugin

internal data class PluginModuleDescriptor(
    val moduleName: String,
    val detectionStrategy: PluginDetectionStrategy
)
