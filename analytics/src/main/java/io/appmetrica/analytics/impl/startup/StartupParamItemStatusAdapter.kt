package io.appmetrica.analytics.impl.startup

import io.appmetrica.analytics.StartupParamsItemStatus
import io.appmetrica.analytics.coreapi.internal.identifiers.IdentifierStatus

class StartupParamItemStatusAdapter {

    fun adapt(input: IdentifierStatus): StartupParamsItemStatus = when (input) {
        IdentifierStatus.OK -> StartupParamsItemStatus.OK
        IdentifierStatus.IDENTIFIER_PROVIDER_UNAVAILABLE -> StartupParamsItemStatus.PROVIDER_UNAVAILABLE
        IdentifierStatus.INVALID_ADV_ID -> StartupParamsItemStatus.INVALID_VALUE_FROM_PROVIDER
        IdentifierStatus.NO_STARTUP -> StartupParamsItemStatus.NETWORK_ERROR
        IdentifierStatus.FEATURE_DISABLED -> StartupParamsItemStatus.FEATURE_DISABLED
        else -> StartupParamsItemStatus.UNKNOWN_ERROR
    }
}
