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

    override fun flush() {
        DebugLogger.info(tag, "Flush all sources")
        sources.forEach { it.second.flush() }
    }

    override fun flushAsync() {
        DebugLogger.info(tag, "FlushAsync all sources")
        sources.forEach { it.second.flushAsync() }
    }
}
