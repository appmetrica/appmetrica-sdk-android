package io.appmetrica.analytics.screenshot.impl.config.remote.model

import io.appmetrica.analytics.screenshot.impl.ServiceCaptorConfigProto

class ServiceCaptorConfig(
    val enabled: Boolean,
    val delaySeconds: Long,
) {

    constructor() : this(
        ServiceCaptorConfigProto().enabled,
        ServiceCaptorConfigProto().delaySeconds
    )

    override fun toString(): String {
        return "ServiceCaptorConfig(" +
            "enabled=$enabled" +
            ", delaySeconds=$delaySeconds" +
            ")"
    }
}
