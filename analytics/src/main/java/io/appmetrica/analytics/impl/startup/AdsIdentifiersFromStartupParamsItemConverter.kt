package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.AdsIdentifiersResult
import io.appmetrica.analytics.StartupParamsItem
import io.appmetrica.analytics.StartupParamsItemStatus

class AdsIdentifiersFromStartupParamsItemConverter {

    fun convert(
        google: StartupParamsItem?,
        hauwei: StartupParamsItem?,
        yandex: StartupParamsItem?
    ): AdsIdentifiersResult = AdsIdentifiersResult(google.toAdvId(), hauwei.toAdvId(), yandex.toAdvId())

    private fun StartupParamsItem?.toAdvId(): AdsIdentifiersResult.AdvId = AdsIdentifiersResult.AdvId(
        this?.id,
        this?.status.toAdsIdentifierResultDetail(),
        this?.errorDetails
    )

    private fun StartupParamsItemStatus?.toAdsIdentifierResultDetail(): AdsIdentifiersResult.Details = when (this) {
        StartupParamsItemStatus.OK -> AdsIdentifiersResult.Details.OK
        StartupParamsItemStatus.NETWORK_ERROR -> AdsIdentifiersResult.Details.NO_STARTUP
        StartupParamsItemStatus.FEATURE_DISABLED -> AdsIdentifiersResult.Details.FEATURE_DISABLED
        StartupParamsItemStatus.PROVIDER_UNAVAILABLE -> AdsIdentifiersResult.Details.IDENTIFIER_PROVIDER_UNAVAILABLE
        StartupParamsItemStatus.INVALID_VALUE_FROM_PROVIDER -> AdsIdentifiersResult.Details.INVALID_ADV_ID
        else -> AdsIdentifiersResult.Details.INTERNAL_ERROR
    }
}
