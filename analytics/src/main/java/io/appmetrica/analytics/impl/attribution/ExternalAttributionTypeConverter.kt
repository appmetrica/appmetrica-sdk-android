package io.appmetrica.analytics.impl.attribution

import io.appmetrica.analytics.impl.protobuf.backend.ExternalAttribution.ClientExternalAttribution

internal object ExternalAttributionTypeConverter {

    fun fromModel(type: ExternalAttributionType): Int {
        return when (type) {
            ExternalAttributionType.UNKNOWN -> ClientExternalAttribution.UNKNOWN
            ExternalAttributionType.APPSFLYER -> ClientExternalAttribution.APPSFLYER
            ExternalAttributionType.ADJUST -> ClientExternalAttribution.ADJUST
            ExternalAttributionType.KOCHAVA -> ClientExternalAttribution.KOCHAVA
            ExternalAttributionType.TENJIN -> ClientExternalAttribution.TENJIN
            ExternalAttributionType.AIRBRIDGE -> ClientExternalAttribution.AIRBRIDGE
            ExternalAttributionType.SINGULAR -> ClientExternalAttribution.SINGULAR
        }
    }

    @JvmStatic
    fun toString(type: Int): String {
        return when (type) {
            ClientExternalAttribution.APPSFLYER -> "APPSFLYER"
            ClientExternalAttribution.ADJUST -> "ADJUST"
            ClientExternalAttribution.KOCHAVA -> "KOCHAVA"
            ClientExternalAttribution.TENJIN -> "TENJIN"
            ClientExternalAttribution.AIRBRIDGE -> "AIRBRIDGE"
            ClientExternalAttribution.SINGULAR -> "SINGULAR"
            else -> "UNKNOWN"
        }
    }
}
