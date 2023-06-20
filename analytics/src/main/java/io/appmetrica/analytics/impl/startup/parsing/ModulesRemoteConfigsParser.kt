package io.appmetrica.analytics.impl.startup.parsing

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.impl.modules.ModuleRemoteConfigController
import io.appmetrica.analytics.impl.utils.JsonHelper.OptJSONObject

internal class ModulesRemoteConfigsParser {

    private val modulesRemoteConfigProcessingExtension = GlobalServiceLocator.getInstance().modulesController

    fun parse(result: StartupResult, response: OptJSONObject) {
        val modulesRemoteConfigControllers: Map<String, ModuleRemoteConfigController> =
            modulesRemoteConfigProcessingExtension.collectRemoteConfigControllers()

        val remoteConfigs =
            modulesRemoteConfigControllers.mapNotNullValues { (_, parser) -> parser.parse(response) }

        result.setModuleRemoteConfigs(remoteConfigs)
    }

    private fun <K, V, R> Map<K, V>.mapNotNullValues(transform: (Map.Entry<K, V>) -> R?): Map<K, R> {
        val destination = LinkedHashMap<K, R>()
        forEach { entry -> transform(entry)?.let { destination[entry.key] = it } }
        return destination
    }
}
