package io.appmetrica.analytics.impl.db

import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

internal class CompositeFileVitalDataSource(
    private val sources: List<Pair<String, VitalDataSource>>
) : VitalDataSource {

    private val tag = "[CompositeFileVitalDataSource]"

    override fun getVitalData(): String? {
        sources.forEach {
            val value = it.second.getVitalData()
            if (value?.isNotEmpty() == true) {
                return value
            } else {
                DebugLogger.info(tag, "File missing or empty: ${it.first}")
            }
        }
        return null
    }

    override fun putVitalData(data: String) {
        sources.forEach { it.second.putVitalData(data) }
    }
}
