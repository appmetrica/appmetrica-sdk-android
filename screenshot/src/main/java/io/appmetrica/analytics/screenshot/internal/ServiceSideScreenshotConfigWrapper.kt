package io.appmetrica.analytics.screenshot.internal

import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig

class ServiceSideScreenshotConfigWrapper internal constructor(
    internal val config: ServiceSideScreenshotConfig
) {
    companion object {
        internal fun ServiceSideScreenshotConfig.toWrapper() = ServiceSideScreenshotConfigWrapper(this)
    }
}
