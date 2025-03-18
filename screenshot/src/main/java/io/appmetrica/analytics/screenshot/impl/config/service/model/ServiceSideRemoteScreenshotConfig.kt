package io.appmetrica.analytics.screenshot.impl.config.service.model

import io.appmetrica.analytics.screenshot.impl.config.remote.model.RemoteScreenshotConfig

class ServiceSideRemoteScreenshotConfig(
    val enabled: Boolean,
    val config: ServiceSideScreenshotConfig?,
) {

    constructor() : this(RemoteScreenshotConfig())

    constructor(remote: RemoteScreenshotConfig) : this(
        remote.enabled,
        remote.config?.let { ServiceSideScreenshotConfig(it) },
    )

    override fun toString(): String {
        return "ServiceSideRemoteScreenshotConfig(" +
            "enabled=$enabled" +
            ", config=$config" +
            ")"
    }
}
