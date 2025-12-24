package io.appmetrica.analytics.coreapi.internal.identifiers

enum class IdentifierStatus(val value: String) {
    OK("OK"),
    IDENTIFIER_PROVIDER_UNAVAILABLE("IDENTIFIER_PROVIDER_UNAVAILABLE"),
    INVALID_ADV_ID("INVALID_ADV_ID"),
    FORBIDDEN_BY_CLIENT_CONFIG("FORBIDDEN_BY_CLIENT_CONFIG"),
    FEATURE_DISABLED("FEATURE_DISABLED"),
    UNKNOWN("UNKNOWN")
    ;

    companion object {
        @JvmStatic
        fun from(stringValue: String?): IdentifierStatus = values().find { it.value == stringValue } ?: UNKNOWN
    }
}
