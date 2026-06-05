package io.appmetrica.analytics.idsync.impl.model

internal data class IdSyncConfig(
    val enabled: Boolean,
    val launchDelay: Long,
    val requests: List<RequestConfig>
)
