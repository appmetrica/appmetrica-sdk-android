package io.appmetrica.analytics.idsync.impl.model

internal data class RequestState(
    val type: String,
    val lastAttempt: Long,
    val lastAttemptResult: RequestAttemptResult
)
