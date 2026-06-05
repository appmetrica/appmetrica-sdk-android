package io.appmetrica.analytics.impl.db

internal data class SessionEventsDeleteParams(
    val sessionId: Long,
    val sessionType: Int,
    val maxNumberInSession: Long,
    val shouldFormCleanupEvent: Boolean,
)
