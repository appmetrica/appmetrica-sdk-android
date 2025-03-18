package io.appmetrica.analytics.screenshot.impl.config.client.model

import io.appmetrica.analytics.screenshot.impl.config.clientservice.model.ParcelableScreenshotConfig

class ClientSideScreenshotConfig(
    val apiCaptorConfig: ClientSideApiCaptorConfig?,
    val serviceCaptorConfig: ClientSideServiceCaptorConfig?,
    val contentObserverCaptorConfig: ClientSideContentObserverCaptorConfig?,
) {

    constructor(remote: ParcelableScreenshotConfig) : this(
        remote.apiCaptorConfig?.let { ClientSideApiCaptorConfig(it) },
        remote.serviceCaptorConfig?.let { ClientSideServiceCaptorConfig(it) },
        remote.contentObserverCaptorConfig?.let { ClientSideContentObserverCaptorConfig(it) },
    )

    override fun toString(): String {
        return "ClientSideScreenshotConfig(" +
            "apiCaptorConfig=$apiCaptorConfig" +
            ", serviceCaptorConfig=$serviceCaptorConfig" +
            ", contentObserverCaptorConfig=$contentObserverCaptorConfig" +
            ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientSideScreenshotConfig

        if (apiCaptorConfig != other.apiCaptorConfig) return false
        if (serviceCaptorConfig != other.serviceCaptorConfig) return false
        if (contentObserverCaptorConfig != other.contentObserverCaptorConfig) return false

        return true
    }

    override fun hashCode(): Int {
        var result = apiCaptorConfig?.hashCode() ?: 0
        result = 31 * result + (serviceCaptorConfig?.hashCode() ?: 0)
        result = 31 * result + (contentObserverCaptorConfig?.hashCode() ?: 0)
        return result
    }
}
