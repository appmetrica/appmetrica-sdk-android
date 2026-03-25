package io.appmetrica.analytics.impl.db

internal class SessionEventsDeleteParams(
    val sessionId: Long,
    val sessionType: Int,
    val maxNumberInSession: Long,
    val shouldFormCleanupEvent: Boolean,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SessionEventsDeleteParams) return false
        return sessionId == other.sessionId &&
            sessionType == other.sessionType &&
            maxNumberInSession == other.maxNumberInSession &&
            shouldFormCleanupEvent == other.shouldFormCleanupEvent
    }

    override fun hashCode(): Int {
        var result = sessionId.hashCode()
        result = 31 * result + sessionType
        result = 31 * result + maxNumberInSession.hashCode()
        result = 31 * result + shouldFormCleanupEvent.hashCode()
        return result
    }
}
