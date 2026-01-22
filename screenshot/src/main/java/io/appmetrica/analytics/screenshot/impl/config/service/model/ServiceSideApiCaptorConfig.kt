package io.appmetrica.analytics.screenshot.impl.config.service.model

import io.appmetrica.analytics.screenshot.impl.config.remote.model.ApiCaptorConfig

internal class ServiceSideApiCaptorConfig(
    val enabled: Boolean,
) {

    constructor(remote: ApiCaptorConfig) : this(remote.enabled)

    override fun toString(): String {
        return "ServiceSideApiCaptorConfig(" +
            "enabled=$enabled" +
            ")"
    }
}
