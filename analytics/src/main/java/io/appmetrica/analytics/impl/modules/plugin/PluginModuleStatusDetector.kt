package io.appmetrica.analytics.impl.modules.plugin

import io.appmetrica.analytics.impl.modules.ModuleStatus

internal class PluginModuleStatusDetector(
    private val descriptors: List<PluginModuleDescriptor>
) {

    fun detect(): List<ModuleStatus> = descriptors.map { descriptor ->
        ModuleStatus(
            moduleName = descriptor.moduleName,
            loaded = descriptor.detectionStrategy.isPresent()
        )
    }
}
