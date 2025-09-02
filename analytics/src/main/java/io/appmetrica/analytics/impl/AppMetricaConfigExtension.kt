package io.appmetrica.analytics.impl

class AppMetricaConfigExtension(
    val autoCollectedDataSubscribers: List<String>,
    val needClearEnvironment: Boolean
) {
    override fun toString(): String {
        return "AppMetricaConfigExtension(autoCollectedDataSubscribers=$autoCollectedDataSubscribers, " +
            "needClearEnvironment=$needClearEnvironment)"
    }
}
