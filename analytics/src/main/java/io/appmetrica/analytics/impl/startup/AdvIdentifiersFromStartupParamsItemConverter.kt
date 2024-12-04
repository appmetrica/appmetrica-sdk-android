package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.AdvIdentifiersResult
import io.appmetrica.analytics.StartupParamsItem
import io.appmetrica.analytics.StartupParamsItemStatus

class AdvIdentifiersFromStartupParamsItemConverter {

    fun convert(
        google: StartupParamsItem?,
        hauwei: StartupParamsItem?,
        yandex: StartupParamsItem?
    ): AdvIdentifiersResult = AdvIdentifiersResult(google.toAdvId(), hauwei.toAdvId(), yandex.toAdvId())

    private fun StartupParamsItem?.toAdvId(): AdvIdentifiersResult.AdvId = AdvIdentifiersResult.AdvId(
        this?.id,
        this?.status.toAdvIdentifierResultDetail(),
        this?.errorDetails
    )

    private fun StartupParamsItemStatus?.toAdvIdentifierResultDetail(): AdvIdentifiersResult.Details = when (this) {
        StartupParamsItemStatus.OK -> AdvIdentifiersResult.Details.OK
        StartupParamsItemStatus.NETWORK_ERROR -> AdvIdentifiersResult.Details.NO_STARTUP
        StartupParamsItemStatus.FEATURE_DISABLED -> AdvIdentifiersResult.Details.FEATURE_DISABLED
        StartupParamsItemStatus.PROVIDER_UNAVAILABLE -> AdvIdentifiersResult.Details.IDENTIFIER_PROVIDER_UNAVAILABLE
        StartupParamsItemStatus.INVALID_VALUE_FROM_PROVIDER -> AdvIdentifiersResult.Details.INVALID_ADV_ID
        StartupParamsItemStatus.FORBIDDEN_BY_CLIENT_CONFIG -> AdvIdentifiersResult.Details.FORBIDDEN_BY_CLIENT_CONFIG
        else -> AdvIdentifiersResult.Details.INTERNAL_ERROR
    }
}
