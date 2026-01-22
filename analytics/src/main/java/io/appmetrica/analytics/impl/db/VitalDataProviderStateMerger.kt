package io.appmetrica.analytics.impl.db

import org.json.JSONObject

internal fun interface VitalDataProviderStateMerger {

    fun merge(primary: JSONObject, backup: JSONObject): JSONObject
}
