package io.appmetrica.analytics.billing.impl.config.remote

import io.appmetrica.analytics.billing.impl.config.remote.converter.BillingConfigProtoConverter
import io.appmetrica.analytics.billing.impl.config.remote.parser.BillingConfigJsonParser
import io.appmetrica.analytics.billing.internal.config.RemoteBillingConfig
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

internal class RemoteBillingConfigParser(
    private val converter: BillingConfigProtoConverter = BillingConfigProtoConverter(),
    private val parser: BillingConfigJsonParser = BillingConfigJsonParser(),
) : JsonParser<RemoteBillingConfig> {

    private val tag = "[RemoteBillingConfigParser]"

    override fun parse(rawData: JSONObject): RemoteBillingConfig {
        DebugLogger.info(tag, "Parsing remote module config")
        val config = converter.toModel(parser.parse(rawData))
        val remoteConfig = RemoteBillingConfig(
            true,
            config
        )
        DebugLogger.info(tag, "Remote module config is '$remoteConfig'")
        return remoteConfig
    }
}
