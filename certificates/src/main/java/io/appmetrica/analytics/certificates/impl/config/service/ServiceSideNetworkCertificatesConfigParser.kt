package io.appmetrica.analytics.certificates.impl.config.service

import io.appmetrica.analytics.certificates.impl.config.service.parser.NetworkCertificatesConfigJsonParser
import io.appmetrica.analytics.certificates.internal.ServiceSideNetworkCertificatesConfigWrapper
import io.appmetrica.analytics.certificates.internal.ServiceSideNetworkCertificatesConfigWrapper.Companion.toWrapper
import io.appmetrica.analytics.coreapi.internal.data.JsonParser
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

internal class ServiceSideNetworkCertificatesConfigParser(
    private val parser: NetworkCertificatesConfigJsonParser = NetworkCertificatesConfigJsonParser(),
) : JsonParser<ServiceSideNetworkCertificatesConfigWrapper> {

    private val tag = "[ServiceSideNetworkCertificatesConfigParser]"

    override fun parse(rawData: JSONObject): ServiceSideNetworkCertificatesConfigWrapper {
        DebugLogger.info(tag, "Parsing remote module config")
        return parser.parse(rawData).toWrapper()
    }
}
