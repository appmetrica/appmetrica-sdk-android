package io.appmetrica.analytics.screenshot.impl.config.client.model

internal data class ClientSideScreenshotConfig(
    val enabled: Boolean,
    val apiCaptorConfig: ClientSideApiCaptorConfig?,
    val serviceCaptorConfig: ClientSideServiceCaptorConfig?,
    val contentObserverCaptorConfig: ClientSideContentObserverCaptorConfig?,
)
