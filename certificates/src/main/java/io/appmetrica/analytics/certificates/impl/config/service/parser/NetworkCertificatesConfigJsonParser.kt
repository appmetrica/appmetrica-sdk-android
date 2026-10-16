package io.appmetrica.analytics.certificates.impl.config.service.parser

import io.appmetrica.analytics.certificates.impl.Constants
import io.appmetrica.analytics.certificates.impl.config.service.model.ServiceSideNetworkCertificatesConfig
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils.extractFeature
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import org.json.JSONObject

internal class NetworkCertificatesConfigJsonParser {

    private val tag = "[NetworkCertificatesConfigJsonParser]"

    fun parse(rawData: JSONObject): ServiceSideNetworkCertificatesConfig {
        DebugLogger.info(tag, "Parsing network certificates config")
        val enabled = extractFeature(
            rawData,
            Constants.RemoteConfig.FEATURE_NAME,
            Constants.Defaults.FEATURE_ENABLED
        )
        return ServiceSideNetworkCertificatesConfig(enabled = enabled)
    }
}
