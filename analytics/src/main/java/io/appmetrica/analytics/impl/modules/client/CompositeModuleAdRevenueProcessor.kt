package io.appmetrica.analytics.impl.modules.client

import io.appmetrica.analytics.coreutils.internal.logger.LoggerStorage
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessor
import io.appmetrica.analytics.modulesapi.internal.client.adrevenue.ModuleAdRevenueProcessorsHolder

class CompositeModuleAdRevenueProcessor :
    ModuleAdRevenueProcessor,
    ModuleAdRevenueProcessorsHolder {

    private val processors = mutableListOf<ModuleAdRevenueProcessor>()

    override fun process(vararg values: Any): Boolean {
        val foundProcessor = processors.firstOrNull {
            it.process(*values)
        }
        val processed = foundProcessor != null
        if (!processed) {
            LoggerStorage.getMainPublicOrAnonymousLogger().info(
                "Ad Revenue was not processed by ${getDescription()} " +
                    "since processor for ${values.contentToString()} was not found"
            )
        }
        return processed
    }

    override fun getDescription() = processors.joinToString(
        prefix = "Composite processor with ${processors.size} children: [",
        postfix = "]"
    ) { it.getDescription() }

    override fun register(processor: ModuleAdRevenueProcessor) {
        processors.add(processor)
    }
}
