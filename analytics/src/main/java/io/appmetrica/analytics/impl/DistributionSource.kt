package io.appmetrica.analytics.impl

internal enum class DistributionSource(val description: String) {

    UNDEFINED("UNDEFINED"),
    APP("APP"),
    SATELLITE("SATELLITE"),
    RETAIL("RETAIL");

    companion object {
        @JvmStatic
        fun fromString(value: String?): DistributionSource = values().find { it.description == value } ?: UNDEFINED
    }
}
