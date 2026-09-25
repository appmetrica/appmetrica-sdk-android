package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

/**
 * Service-owned ordinary-activation flag for `hoyas`, backed by service preferences DB.
 *
 * Anonymous apiKey clears previously observed ordinary activation.
 */
internal class YandexAdsOrdinaryActivationState(
    private val storage: PreferencesServiceDbStorage
) {

    private val tag = "[YandexAdsOrdinaryActivationState]"

    @Volatile
    private var hasOrdinaryActivation: Boolean = storage.hasOrdinaryActivation()

    init {
        DebugLogger.info(tag, "Restored ordinary flag from storage: $hasOrdinaryActivation")
    }

    @Synchronized
    fun onMainClientApiKey(apiKey: String?) {
        when (apiKey) {
            null -> {
                DebugLogger.info(tag, "Main client apiKey is null; ordinary flag unchanged=$hasOrdinaryActivation")
            }

            DefaultValues.ANONYMOUS_API_KEY -> {
                val wasOrdinary = hasOrdinaryActivation
                setOrdinaryActivation(false)
                DebugLogger.info(
                    tag,
                    "Mark anonymous activation from anonymous api. wasOrdinary=$wasOrdinary"
                )
            }

            else -> {
                val wasOrdinary = hasOrdinaryActivation
                setOrdinaryActivation(true)
                DebugLogger.info(
                    tag,
                    "Mark ordinary activation from main client apiKey. wasOrdinary=$wasOrdinary"
                )
            }
        }
    }

    fun hasOrdinaryActivation(): Boolean = hasOrdinaryActivation

    private fun setOrdinaryActivation(value: Boolean) {
        if (hasOrdinaryActivation == value) {
            return
        }
        hasOrdinaryActivation = value
        storage.saveOrdinaryActivation(value)
    }
}
