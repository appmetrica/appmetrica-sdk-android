package io.appmetrica.analytics.coreapi.internal.identifiers

enum class IdentifierStatus(val value: String) {
    OK("OK"),
    IDENTIFIER_PROVIDER_UNAVAILABLE("IDENTIFIER_PROVIDER_UNAVAILABLE"),
    INVALID_ADV_ID("INVALID_ADV_ID"),
    NO_STARTUP("NO_STARTUP"),
    FEATURE_DISABLED("FEATURE_DISABLED"),
    UNKNOWN("UNKNOWN")
    ;

    companion object {
        @JvmStatic
        fun from(stringValue: String?): IdentifierStatus = values().find { it.value == stringValue } ?: UNKNOWN
    }
}
