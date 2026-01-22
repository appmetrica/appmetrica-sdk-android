package io.appmetrica.analytics.screenshot.impl.config.client.model

import io.appmetrica.analytics.screenshot.impl.config.clientservice.model.ParcelableContentObserverCaptorConfig

internal class ClientSideContentObserverCaptorConfig(
    val enabled: Boolean,
    val mediaStoreColumnNames: List<String>,
    val detectWindowSeconds: Long,
) {

    constructor(remote: ParcelableContentObserverCaptorConfig) : this(
        remote.enabled,
        remote.mediaStoreColumnNames,
        remote.detectWindowSeconds,
    )

    override fun toString(): String {
        return "ClientSideContentObserverCaptorConfig(" +
            "enabled=$enabled" +
            ", mediaStoreColumnNames=$mediaStoreColumnNames" +
            ", detectWindowSeconds=$detectWindowSeconds" +
            ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientSideContentObserverCaptorConfig

        if (enabled != other.enabled) return false
        if (mediaStoreColumnNames != other.mediaStoreColumnNames) return false
        if (detectWindowSeconds != other.detectWindowSeconds) return false

        return true
    }

    override fun hashCode(): Int {
        var result = enabled.hashCode()
        result = 31 * result + mediaStoreColumnNames.hashCode()
        result = 31 * result + detectWindowSeconds.hashCode()
        return result
    }
}
