package io.appmetrica.analytics.impl.startup

import android.content.Context
import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

/**
 * Computes Ads-only state for startup (`hoyas`).
 */
internal class YandexAdsStartupStateProvider(
    private val detector: YandexAdsSDKDetector,
    private val ordinaryActivationState: YandexAdsOrdinaryActivationState
) {

    private val tag = "[YandexAdsStartupStateProvider]"

    /**
     * `true` when Ads marker is present and there was no ordinary main-client activation
     * in this service process. Otherwise `false`.
     */
    val isYandexAdsOnly: Boolean
        get() {
            val adsMarkerPresent = detector.isPresent
            if (!adsMarkerPresent) {
                DebugLogger.info(
                    tag,
                    "hoyas current=false: Ads marker class is absent"
                )
                return false
            }
            val hasOrdinary = ordinaryActivationState.hasOrdinaryActivation()
            val result = !hasOrdinary
            DebugLogger.info(
                tag,
                "hoyas current=$result: Ads marker present, hasOrdinaryActivation=$hasOrdinary"
            )
            return result
        }

    /**
     * Whether a startup is needed for `hoyas`: force when never sent and [current] is `true`,
     * or when [lastSent] differs from [current]; otherwise no.
     */
    fun requiresUpdate(current: Boolean, lastSent: Boolean?): Boolean {
        val required = if (lastSent == null) {
            current
        } else {
            lastSent != current
        }
        DebugLogger.info(
            tag,
            "hoyas requiresUpdate=$required: current=$current, lastSent=$lastSent"
        )
        return required
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun create(
            context: Context,
            detector: YandexAdsSDKDetector = YandexAdsSDKDetector.shared
        ): YandexAdsStartupStateProvider {
            return YandexAdsStartupStateProvider(
                detector,
                GlobalServiceLocator.getInstance().yandexAdsOrdinaryActivationState
            )
        }
    }
}
