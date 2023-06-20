package io.appmetrica.analytics.impl

enum class CounterConfigurationReporterType(val stringValue: String) {

    MAIN("main"),
    MANUAL("manual"),
    SELF_SDK("self_sdk"),
    COMMUTATION("commutation"),
    SELF_DIAGNOSTIC_MAIN("self_diagnostic_main"),
    SELF_DIAGNOSTIC_MANUAL("self_diagnostic_manual"),
    CRASH("crash");

    companion object {

        @JvmStatic
        fun fromStringValue(value: String?) =
            values().firstOrNull { it.stringValue == value } ?: MAIN
    }
}
