package io.appmetrica.analytics.screenshot.impl.config.client.model

internal data class ClientSideContentObserverCaptorConfig(
    val enabled: Boolean,
    val mediaStoreColumnNames: List<String>,
    val detectWindowSeconds: Long,
)
