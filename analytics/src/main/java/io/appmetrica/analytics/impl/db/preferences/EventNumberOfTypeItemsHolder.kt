package io.appmetrica.analytics.impl.db.preferences

import io.appmetrica.analytics.impl.db.VitalComponentDataProvider
import org.json.JSONObject

internal class EventNumberOfTypeItemsHolder(
    private val vitalComponentDataProvider: VitalComponentDataProvider
) {

    fun getNumberOfType(type: Int): Long {
        return vitalComponentDataProvider.numbersOfType?.optLong("$type") ?: 0L
    }

    fun putNumberOfType(type: Int, number: Long) {
        val json = vitalComponentDataProvider.numbersOfType ?: JSONObject()
        json.put("$type", number)
        vitalComponentDataProvider.numbersOfType = json
    }
}
