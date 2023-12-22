package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.coreutils.internal.logger.YLogger
import io.appmetrica.analytics.coreutils.internal.parsing.isEqualTo
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.impl.component.ComponentUnit
import org.json.JSONObject

class ExternalAttributionHelper(
    private val component: ComponentUnit,
    private val timeProvider: TimeProvider
) {

    fun isInAttributionCollectingWindow(): Boolean {
        val collectingInterval = component.startupState.externalAttributionConfig?.collectingInterval

        if (collectingInterval != null) {
            val externalAttributionWindowStart = getExternalAttributionWindowStartAndSetIfAbsent()
            if (timeProvider.currentTimeMillis() - externalAttributionWindowStart > collectingInterval) {
                return false
            }
        } else {
            YLogger.e("Attribution collecting interval is null")
            return false
        }
        return true
    }

    fun isNewAttribution(
        attributionType: Int,
        data: String
    ): Boolean {
        val attributions = component.componentPreferences.sentExternalAttributions
        val prevValue = attributions[attributionType]
        return (prevValue == null) || try {
            !JSONObject(data).isEqualTo(JSONObject(prevValue))
        } catch (e: Throwable) {
            YLogger.e(e, "Failed to parse attribution")
            true
        }
    }

    fun saveAttribution(
        attributionType: Int,
        data: String
    ) {
        val attributions = component.componentPreferences.sentExternalAttributions
        attributions[attributionType] = data
        component.componentPreferences.putSentExternalAttributions(attributions)
    }

    private fun getExternalAttributionWindowStartAndSetIfAbsent(): Long {
        var result = component.vitalComponentDataProvider.externalAttributionWindowStart
        if (result < 0) {
            result = timeProvider.currentTimeMillis()
            component.vitalComponentDataProvider.externalAttributionWindowStart = result
        }
        return result
    }
}
