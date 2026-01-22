package io.appmetrica.analytics.screenshot.impl.config.client.model

import io.appmetrica.analytics.screenshot.internal.config.ParcelableRemoteScreenshotConfig

internal class ClientSideRemoteScreenshotConfig(
    val enabled: Boolean,
    val config: ClientSideScreenshotConfig?,
) {

    constructor(remote: ParcelableRemoteScreenshotConfig) : this(
        remote.enabled,
        remote.config?.let { ClientSideScreenshotConfig(it) },
    )

    override fun toString(): String {
        return "ClientSideRemoteScreenshotConfig(" +
            "enabled=$enabled" +
            ", config=$config" +
            ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientSideRemoteScreenshotConfig

        if (enabled != other.enabled) return false
        if (config != other.config) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + (config?.hashCode() ?: 0)
        return result
    }
}
