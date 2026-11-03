package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.impl.DefaultValues
import io.appmetrica.analytics.impl.db.preferences.PreferencesServiceDbStorage
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

/**
 * Service-owned ordinary-activation flag for `hoyas`, backed by service preferences DB.
 *
 * Client activation takes precedence over storage; ordinary client activation cannot be downgraded.
 */
internal class YandexAdsOrdinaryActivationState(
    private val storage: PreferencesServiceDbStorage
) {

    private val tag = "[YandexAdsOrdinaryActivationState]"
    private val stateFromStorage = storage.hasOrdinaryActivation()

    @Volatile
    private var stateFromClient: Boolean? = null

    init {
        DebugLogger.info(tag, "Restored ordinary flag from storage: $stateFromStorage")
    }

    @Synchronized
    fun onMainClientApiKey(apiKey: String?) {
        if (apiKey == null) {
            DebugLogger.info(
                tag,
                "Ignore missing main client API key: client=$stateFromClient, " +
                    "storedAtStart=$stateFromStorage, effective=${hasOrdinaryActivation()}"
            )
            return
        }
        val previousClient = stateFromClient
        val incomingOrdinary = apiKey != DefaultValues.ANONYMOUS_API_KEY
        val previous = hasOrdinaryActivation()
        stateFromClient = previousClient == true || incomingOrdinary
        val current = hasOrdinaryActivation()
        if (previous != current) {
            storage.saveOrdinaryActivation(current)
        }
        DebugLogger.info(
            tag,
            "Main client activation: incomingOrdinary=$incomingOrdinary, " +
                "client=$previousClient -> $stateFromClient, storedAtStart=$stateFromStorage, " +
                "effective=$previous -> $current, storageWriteRequested=${previous != current}, " +
                "anonymousIgnoredByOrdinaryPriority=${previousClient == true && !incomingOrdinary}"
        )
    }

    fun hasOrdinaryActivation(): Boolean = stateFromClient ?: stateFromStorage
}
