package io.appmetrica.analytics.coreutils.internal.services

import io.appmetrica.analytics.logger.internal.YLogger

class UtilityServiceProvider {

    private val tag = "[UtilityServiceProvider]"

    val firstExecutionService: FirstExecutionConditionServiceImpl by lazy { FirstExecutionConditionServiceImpl(this) }

    val activationBarrier = WaitForActivationDelayBarrier()

    fun initAsync() {
        activationBarrier.activate()
    }

    fun updateConfiguration(configuration: UtilityServiceConfiguration) {
        YLogger.info(tag, "updateConfiguration: $configuration")
        firstExecutionService.updateConfig(configuration)
    }
}
