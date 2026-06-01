package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.converter

import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.model.ServiceApplovinConfig
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.protobuf.client.AdRevenueApplovinConfigProtobuf
import io.appmetrica.analytics.coreapi.internal.data.ProtobufConverter

internal class ApplovinConfigProtoConverter :
    ProtobufConverter<ServiceApplovinConfig, AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig> {

    override fun fromModel(
        value: ServiceApplovinConfig
    ): AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig {
        return AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig().also { proto ->
            proto.enabled = value.enabled
        }
    }

    override fun toModel(
        value: AdRevenueApplovinConfigProtobuf.AdRevenueApplovinConfig
    ): ServiceApplovinConfig {
        return ServiceApplovinConfig(
            enabled = value.enabled,
        )
    }
}
