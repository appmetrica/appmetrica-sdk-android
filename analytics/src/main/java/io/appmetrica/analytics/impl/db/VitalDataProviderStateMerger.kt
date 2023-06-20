package io.appmetrica.analytics.impl.db

import org.json.JSONObject

fun interface VitalDataProviderStateMerger {

    fun merge(primary: JSONObject, backup: JSONObject): JSONObject
}
