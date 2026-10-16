package io.appmetrica.analytics.certificates.impl.config.service.converter

import io.appmetrica.analytics.certificates.impl.RemoteNetworkCertificatesConfigProto
import io.appmetrica.analytics.certificates.impl.config.service.model.ServiceSideNetworkCertificatesConfig
import io.appmetrica.analytics.coreapi.internal.data.Converter

internal class NetworkCertificatesConfigProtoConverter :
    Converter<ServiceSideNetworkCertificatesConfig, RemoteNetworkCertificatesConfigProto> {

    override fun fromModel(value: ServiceSideNetworkCertificatesConfig): RemoteNetworkCertificatesConfigProto {
        return RemoteNetworkCertificatesConfigProto().also { proto ->
            proto.enabled = value.enabled
        }
    }

    override fun toModel(value: RemoteNetworkCertificatesConfigProto): ServiceSideNetworkCertificatesConfig {
        return ServiceSideNetworkCertificatesConfig(enabled = value.enabled)
    }
}
