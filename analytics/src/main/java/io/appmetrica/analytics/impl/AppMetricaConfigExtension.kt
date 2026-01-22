package io.appmetrica.analytics.impl

internal class AppMetricaConfigExtension(
    val autoCollectedDataSubscribers: List<String>,
    val needClearEnvironment: Boolean
) {
    override fun toString(): String {
        return "AppMetricaConfigExtension(autoCollectedDataSubscribers=$autoCollectedDataSubscribers, " +
            "needClearEnvironment=$needClearEnvironment)"
    }
}
