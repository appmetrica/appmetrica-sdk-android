package io.appmetrica.analytics.impl.proxy.validation

import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.impl.utils.LoggerStorage

class ConfigChecker(
    apiKey: String
) {

    private val logger = LoggerStorage.getOrCreatePublicLogger(apiKey)

    fun getCheckedMaxReportsInDatabaseCount(value: Int): Int {
        if (value < DefaultValues.DEFAULT_MAX_REPORTS_COUNT_LOWER_BOUND) {
            logger.fw(
                "Value passed as maxReportsInDatabaseCount is invalid. " +
                    "Should be greater than or equal to ${DefaultValues.DEFAULT_MAX_REPORTS_COUNT_LOWER_BOUND}, " +
                    "but was: $value. " +
                    "Default value (${DefaultValues.DEFAULT_MAX_REPORTS_COUNT_LOWER_BOUND}) will be used"
            )
            return DefaultValues.DEFAULT_MAX_REPORTS_COUNT_LOWER_BOUND
        }
        if (value > DefaultValues.DEFAULT_MAX_REPORTS_COUNT_UPPER_BOUND) {
            logger.fw(
                "Value passed as maxReportsInDatabaseCount is invalid. " +
                    "Should be less than or equal to ${DefaultValues.DEFAULT_MAX_REPORTS_COUNT_UPPER_BOUND}, " +
                    "but was: $value. " +
                    "Default value (${DefaultValues.DEFAULT_MAX_REPORTS_COUNT_UPPER_BOUND}) will be used"
            )
            return DefaultValues.DEFAULT_MAX_REPORTS_COUNT_UPPER_BOUND
        }
        return value
    }
}
