package io.appmetrica.analytics.adrevenue.other.impl.config.service.converter

import io.appmetrica.analytics.adrevenue.other.impl.AdRevenueOtherConfigProto
import io.appmetrica.analytics.adrevenue.other.impl.config.service.model.ServiceSideAdRevenueOtherConfig
import io.appmetrica.analytics.coreapi.internal.data.Converter

internal class AdRevenueOtherConfigProtoConverter :
    Converter<ServiceSideAdRevenueOtherConfig, AdRevenueOtherConfigProto> {

    override fun fromModel(value: ServiceSideAdRevenueOtherConfig): AdRevenueOtherConfigProto {
        return AdRevenueOtherConfigProto().also { proto ->
            proto.enabled = value.enabled
            proto.includeSource = value.includeSource
        }
    }

    override fun toModel(value: AdRevenueOtherConfigProto): ServiceSideAdRevenueOtherConfig {
        return ServiceSideAdRevenueOtherConfig(
            enabled = value.enabled,
            includeSource = value.includeSource,
        )
    }
}
