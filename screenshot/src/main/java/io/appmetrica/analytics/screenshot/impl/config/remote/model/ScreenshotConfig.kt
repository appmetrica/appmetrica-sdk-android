package io.appmetrica.analytics.screenshot.impl.config.remote.model

class ScreenshotConfig(
    val apiCaptorConfig: ApiCaptorConfig?,
    val serviceCaptorConfig: ServiceCaptorConfig?,
    val contentObserverCaptorConfig: ContentObserverCaptorConfig?,
) {

    constructor() : this(
        ApiCaptorConfig(),
        ServiceCaptorConfig(),
        ContentObserverCaptorConfig()
    )

    override fun toString(): String {
        return "ScreenshotConfig(" +
            "apiCaptorConfig=$apiCaptorConfig" +
            ", serviceCaptorConfig=$serviceCaptorConfig" +
            ", contentObserverCaptorConfig=$contentObserverCaptorConfig" +
            ")"
    }
}
