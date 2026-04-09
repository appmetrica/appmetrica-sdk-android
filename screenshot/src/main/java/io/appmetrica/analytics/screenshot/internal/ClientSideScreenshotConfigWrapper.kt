package io.appmetrica.analytics.screenshot.internal

import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig

class ClientSideScreenshotConfigWrapper internal constructor(
    internal val config: ClientSideScreenshotConfig
) {
    companion object {
        internal fun ClientSideScreenshotConfig.toWrapper() = ClientSideScreenshotConfigWrapper(this)
    }
}
