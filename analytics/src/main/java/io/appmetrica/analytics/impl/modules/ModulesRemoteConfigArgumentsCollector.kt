package io.appmetrica.analytics.impl.modules

interface ModulesRemoteConfigArgumentsCollector {

    fun collectFeatures(): List<String>
    fun collectBlocks(): Map<String, Int>
}
