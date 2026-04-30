package io.appmetrica.analytics.adrevenue.other.impl.config.service

import io.appmetrica.analytics.adrevenue.other.impl.config.service.parser.AdRevenueOtherConfigJsonParser
import io.appmetrica.analytics.adrevenue.other.internal.ServiceSideAdRevenueOtherConfigWrapper
import io.appmetrica.analytics.adrevenue.other.internal.ServiceSideAdRevenueOtherConfigWrapper.Companion.toWrapper
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

internal class ServiceSideAdRevenueOtherConfigParser(
    private val parser: AdRevenueOtherConfigJsonParser = AdRevenueOtherConfigJsonParser(),
) : JsonParser<ServiceSideAdRevenueOtherConfigWrapper> {

    private val tag = "[ServiceSideAdRevenueOtherConfigParser]"

    override fun parse(rawData: JSONObject): ServiceSideAdRevenueOtherConfigWrapper {
        DebugLogger.info(tag, "Parsing remote module config")
        val config = parser.parse(rawData)
        DebugLogger.info(tag, "Remote module config is '$config'")
        return config.toWrapper()
    }
}
