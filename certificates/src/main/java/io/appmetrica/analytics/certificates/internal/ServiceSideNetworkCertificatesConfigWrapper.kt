package io.appmetrica.analytics.certificates.internal

import io.appmetrica.analytics.certificates.impl.config.service.model.ServiceSideNetworkCertificatesConfig

class ServiceSideNetworkCertificatesConfigWrapper internal constructor(
    internal val config: ServiceSideNetworkCertificatesConfig
) {
    companion object {
        internal fun ServiceSideNetworkCertificatesConfig.toWrapper() =
            ServiceSideNetworkCertificatesConfigWrapper(this)
    }
}
