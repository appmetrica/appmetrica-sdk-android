package io.appmetrica.analytics.impl.modules

internal interface ModulesRemoteConfigArgumentsCollector {

    fun collectFeatures(): List<String>
    fun collectBlocks(): Map<String, Int>
}
