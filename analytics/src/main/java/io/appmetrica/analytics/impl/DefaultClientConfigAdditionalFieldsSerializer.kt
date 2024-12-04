package io.appmetrica.analytics.impl

import io.appmetrica.analytics.AppMetricaConfig
import org.json.JSONObject

class DefaultClientConfigAdditionalFieldsSerializer : ClientConfigAdditionalFieldsSerializer {

    override fun toJson(additionalFields: Map<String, Any?>): JSONObject = JSONObject()

    override fun parseJson(additionalFieldsJson: JSONObject?, to: AppMetricaConfig.Builder) {
        // Do nothing
    }
}
