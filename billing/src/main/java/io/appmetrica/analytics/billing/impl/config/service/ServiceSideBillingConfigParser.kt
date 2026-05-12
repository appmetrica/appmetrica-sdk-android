package io.appmetrica.analytics.billing.impl.config.service

import io.appmetrica.analytics.billing.impl.config.service.converter.BillingConfigProtoConverter
import io.appmetrica.analytics.billing.impl.config.service.model.ServiceSideRemoteBillingConfig
import io.appmetrica.analytics.billing.impl.config.service.parser.BillingConfigJsonParser
import io.appmetrica.analytics.billing.internal.ServiceSideBillingConfigWrapper
import io.appmetrica.analytics.billing.internal.ServiceSideBillingConfigWrapper.Companion.toWrapper
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

internal class ServiceSideBillingConfigParser(
    private val converter: BillingConfigProtoConverter = BillingConfigProtoConverter(),
    private val parser: BillingConfigJsonParser = BillingConfigJsonParser(),
) : JsonParser<ServiceSideBillingConfigWrapper> {

    private val tag = "[ServiceSideBillingConfigParser]"

    override fun parse(rawData: JSONObject): ServiceSideBillingConfigWrapper {
        DebugLogger.info(tag, "Parsing remote module config")
        val config = converter.toModel(parser.parse(rawData))
        val remoteConfig = ServiceSideRemoteBillingConfig(
            true,
            config
        )
        DebugLogger.info(tag, "Remote module config is '$remoteConfig'")
        return remoteConfig.toWrapper()
    }
}
