package io.appmetrica.analytics.idsync.internal.model

class IdSyncConfig(
    val enabled: Boolean,
    val launchDelay: Long,
    val requests: List<RequestConfig>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IdSyncConfig

        if (enabled != other.enabled) return false
        if (launchDelay != other.launchDelay) return false
        if (requests != other.requests) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + launchDelay.hashCode()
        result = 31 * result + requests.hashCode()
        return result
    }

    override fun toString(): String {
        return "IdSyncConfig(enabled=$enabled, launchDelay=$launchDelay, requests=$requests)"
    }
}
