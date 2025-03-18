package io.appmetrica.analytics.screenshot.impl.config.remote.model

import io.appmetrica.analytics.screenshot.impl.ApiCaptorConfigProto

class ApiCaptorConfig(
    val enabled: Boolean,
) {

    constructor() : this(ApiCaptorConfigProto().enabled)

    override fun toString(): String {
        return "ApiCaptorConfig(" +
            "enabled=$enabled" +
            ")"
    }
}
