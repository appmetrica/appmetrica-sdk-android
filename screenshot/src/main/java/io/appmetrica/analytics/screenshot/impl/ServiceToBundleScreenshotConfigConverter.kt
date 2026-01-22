package io.appmetrica.analytics.screenshot.impl

import android.os.Bundle
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideRemoteScreenshotConfig
import io.appmetrica.analytics.screenshot.internal.config.ParcelableRemoteScreenshotConfig

internal class ServiceToBundleScreenshotConfigConverter {

    private val tag = "[ServiceToBundleScreenshotConfigConverter]"

    fun convert(config: ServiceSideRemoteScreenshotConfig?): Bundle? {
        DebugLogger.info(tag, "convert $config")
        if (config == null) {
            return null
        }
        val bundle = Bundle()
        bundle.putParcelable(Constants.ParcelableConfig.CONFIG, ParcelableRemoteScreenshotConfig(config))

        return bundle
    }
}
