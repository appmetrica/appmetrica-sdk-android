package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import org.json.JSONObject

internal interface ClientConfigAdditionalFieldsSerializer {
    fun toJson(additionalFields: Map<String, Any?>): JSONObject
    fun parseJson(additionalFieldsJson: JSONObject?, to: AppMetricaConfig.Builder)
}
