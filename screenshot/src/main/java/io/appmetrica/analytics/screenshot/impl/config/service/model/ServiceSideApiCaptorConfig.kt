package io.appmetrica.analytics.screenshot.impl.config.service.model

import io.appmetrica.analytics.screenshot.impl.ApiCaptorConfigProto

internal class ServiceSideApiCaptorConfig(
    val enabled: Boolean,
) {

    constructor() : this(ApiCaptorConfigProto().enabled)

    override fun toString(): String {
        return "ServiceSideApiCaptorConfig(" +
            "enabled=$enabled" +
            ")"
    }
}
