package io.appmetrica.analytics.coreutils.internal.services

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class UtilityServiceProvider {

    private val tag = "[UtilityServiceProvider]"

    val firstExecutionService: FirstExecutionConditionServiceImpl by lazy { FirstExecutionConditionServiceImpl(this) }

    val activationBarrier = WaitForActivationDelayBarrier()

    fun initAsync() {
        activationBarrier.activate()
    }

    fun updateConfiguration(configuration: UtilityServiceConfiguration) {
        DebugLogger.info(tag, "updateConfiguration: $configuration")
        firstExecutionService.updateConfig(configuration)
    }
}
