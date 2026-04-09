package io.appmetrica.analytics.screenshot.impl.config.service.model

import io.appmetrica.analytics.screenshot.impl.RemoteScreenshotConfigProto

internal class ServiceSideScreenshotConfig(
    val enabled: Boolean,
    val apiCaptorConfig: ServiceSideApiCaptorConfig?,
    val serviceCaptorConfig: ServiceSideServiceCaptorConfig?,
    val contentObserverCaptorConfig: ServiceSideContentObserverCaptorConfig?,
) {

    constructor() : this(
        RemoteScreenshotConfigProto().enabled,
        ServiceSideApiCaptorConfig(),
        ServiceSideServiceCaptorConfig(),
        ServiceSideContentObserverCaptorConfig()
    )

    override fun toString(): String {
        return "ServiceSideScreenshotConfig(" +
            "enabled=$enabled" +
            ", apiCaptorConfig=$apiCaptorConfig" +
            ", serviceCaptorConfig=$serviceCaptorConfig" +
            ", contentObserverCaptorConfig=$contentObserverCaptorConfig" +
            ")"
    }
}
