package io.appmetrica.analytics.impl.startup

internal data class ExpectedStartupResults(
    val containsIdentifiers: Boolean,
    val shouldSendStartupForAll: Boolean,
    val shouldSendStartup: Boolean
)
