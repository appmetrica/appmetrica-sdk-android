package io.appmetrica.analytics.screenshot.impl.config.client.model

import io.appmetrica.analytics.screenshot.impl.config.clientservice.model.ParcelableServiceCaptorConfig

internal class ClientSideServiceCaptorConfig(
    val enabled: Boolean,
    val delaySeconds: Long,
) {

    constructor(remote: ParcelableServiceCaptorConfig) : this(
        remote.enabled,
        remote.delaySeconds,
    )

    override fun toString(): String {
        return "ClientSideServiceCaptorConfig(" +
            "enabled=$enabled" +
            ", delaySeconds=$delaySeconds" +
            ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientSideServiceCaptorConfig

        if (enabled != other.enabled) return false
        if (delaySeconds != other.delaySeconds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + delaySeconds.hashCode()
        return result
    }
}
