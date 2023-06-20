package io.appmetrica.analytics.impl.modules

internal interface ModulesRemoteConfigProcessingExtension {

    fun collectRemoteConfigControllers(): Map<String, ModuleRemoteConfigController>
}
