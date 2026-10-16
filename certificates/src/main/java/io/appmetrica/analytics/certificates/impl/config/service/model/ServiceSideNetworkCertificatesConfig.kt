package io.appmetrica.analytics.certificates.impl.config.service.model

import io.appmetrica.analytics.certificates.impl.Constants

internal class ServiceSideNetworkCertificatesConfig(
    val enabled: Boolean = Constants.Defaults.FEATURE_ENABLED,
) {
    override fun toString(): String {
        return "ServiceSideNetworkCertificatesConfig(enabled=$enabled)"
    }
}
