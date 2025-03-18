package io.appmetrica.analytics.screenshot.impl.config.service.model

import io.appmetrica.analytics.screenshot.impl.config.remote.model.ScreenshotConfig

class ServiceSideScreenshotConfig(
    val apiCaptorConfig: ServiceSideApiCaptorConfig?,
    val serviceCaptorConfig: ServiceSideServiceCaptorConfig?,
    val contentObserverCaptorConfig: ServiceSideContentObserverCaptorConfig?,
) {

    constructor(remote: ScreenshotConfig) : this(
        remote.apiCaptorConfig?.let { ServiceSideApiCaptorConfig(it) },
        remote.serviceCaptorConfig?.let { ServiceSideServiceCaptorConfig(it) },
        remote.contentObserverCaptorConfig?.let { ServiceSideContentObserverCaptorConfig(it) },
    )

    override fun toString(): String {
        return "ServiceSideScreenshotConfig(" +
            "apiCaptorConfig=$apiCaptorConfig" +
            ", serviceCaptorConfig=$serviceCaptorConfig" +
            ", contentObserverCaptorConfig=$contentObserverCaptorConfig" +
            ")"
    }
}
