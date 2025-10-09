package io.appmetrica.analytics.billing.impl.config.remote.parser

import io.appmetrica.analytics.billing.impl.BillingConfigProto
import io.appmetrica.analytics.billing.impl.Constants
import io.appmetrica.analytics.coreutils.internal.parsing.JsonUtils.optIntOrNull
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

class BillingConfigJsonParser {

    private val tag = "[BillingConfigJsonParser]"

    fun parse(rawData: JSONObject): BillingConfigProto {
        DebugLogger.info(tag, "Parsing billing config $rawData")
        val json = rawData.optJSONObject(Constants.RemoteConfig.BLOCK_NAME)
            ?: return BillingConfigProto()

        return BillingConfigProto().also { proto ->
            json.optIntOrNull(Constants.RemoteConfig.SEND_FREQUENCY_SECONDS)?.also {
                proto.sendFrequencySeconds = it
            }
            json.optIntOrNull(Constants.RemoteConfig.FIRST_COLLECTING_INAPP_MAX_AGE_SECONDS)?.also {
                proto.firstCollectingInappMaxAgeSeconds = it
            }
        }
    }
}
