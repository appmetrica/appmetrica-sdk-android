package io.appmetrica.analytics.screenshot.internal.config

import io.appmetrica.analytics.screenshot.impl.RemoteScreenshotConfigProto
import io.appmetrica.analytics.screenshot.impl.config.remote.model.ScreenshotConfig

class RemoteScreenshotConfig internal constructor(
    val enabled: Boolean,
    val config: ScreenshotConfig,
) {

    internal constructor() : this(
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
