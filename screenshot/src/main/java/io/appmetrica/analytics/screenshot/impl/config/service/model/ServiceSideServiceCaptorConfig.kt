package io.appmetrica.analytics.screenshot.impl.config.service.model

import io.appmetrica.analytics.screenshot.impl.ServiceCaptorConfigProto

internal class ServiceSideServiceCaptorConfig(
    val enabled: Boolean,
    val delaySeconds: Long,
) {

    constructor() : this(
        ServiceCaptorConfigProto().enabled,
        ServiceCaptorConfigProto().delaySeconds
    )

    override fun toString(): String {
        return "ServiceSideServiceCaptorConfig(" +
            "enabled=$enabled" +
            ", delaySeconds=$delaySeconds" +
            ")"
    }
}
