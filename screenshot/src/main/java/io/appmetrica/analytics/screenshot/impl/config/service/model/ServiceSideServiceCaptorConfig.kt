package io.appmetrica.analytics.screenshot.impl.config.service.model

import io.appmetrica.analytics.screenshot.impl.config.remote.model.ServiceCaptorConfig

class ServiceSideServiceCaptorConfig(
    val enabled: Boolean,
    val delaySeconds: Long,
) {

    constructor(remote: ServiceCaptorConfig) : this(
        remote.enabled,
        remote.delaySeconds,
    )

    override fun toString(): String {
        return "ServiceSideServiceCaptorConfig(" +
            "enabled=$enabled" +
            ", delaySeconds=$delaySeconds" +
            ")"
    }
}
