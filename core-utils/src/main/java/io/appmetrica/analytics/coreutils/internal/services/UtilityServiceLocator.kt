package io.appmetrica.analytics.coreutils.internal.services

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.coreutils.internal.logger.YLogger

class UtilityServiceLocator @VisibleForTesting constructor() {

    companion object {

        private const val TAG = "[UtilityServiceLocator]"

        @Volatile
        @get:JvmStatic
        @set:VisibleForTesting
        var instance: UtilityServiceLocator = UtilityServiceLocator()
    }

    val firstExecutionService: FirstExecutionConditionService by lazy { FirstExecutionConditionService() }

    val activationBarrier = ActivationBarrier()

    fun initAsync() {
        activationBarrier.activate()
    }

    fun updateConfiguration(configuration: UtilityServiceConfiguration) {
        YLogger.info(TAG, "updateConfiguration: $configuration")
        firstExecutionService.updateConfig(configuration)
    }
}
