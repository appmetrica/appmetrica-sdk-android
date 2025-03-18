package io.appmetrica.analytics.screenshot.impl.config.client.model

import io.appmetrica.analytics.screenshot.impl.config.clientservice.model.ParcelableApiCaptorConfig

class ClientSideApiCaptorConfig(
    val enabled: Boolean,
) {

    constructor(parcelable: ParcelableApiCaptorConfig) : this(parcelable.enabled)

    override fun toString(): String {
        return "ClientSideApiCaptorConfig(" +
            "enabled=$enabled" +
            ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientSideApiCaptorConfig

        return enabled == other.enabled
    }

    override fun hashCode(): Int {
        return enabled.hashCode()
    }
}
