package io.appmetrica.analytics.screenshot.impl.config.remote.model

import io.appmetrica.analytics.screenshot.impl.RemoteScreenshotConfigProto

class RemoteScreenshotConfig(
    val enabled: Boolean,
    val config: ScreenshotConfig,
) {

    constructor() : this(
        RemoteScreenshotConfigProto().enabled,
        ScreenshotConfig(),
    )

    override fun toString(): String {
        return "RemoteScreenshotConfig(" +
            "enabled=$enabled" +
            ", config=$config" +
            ")"
    }
}
