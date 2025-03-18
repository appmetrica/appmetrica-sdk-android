package io.appmetrica.analytics.screenshot.impl

import android.os.Bundle
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.BundleToServiceConfigConverter
import io.appmetrica.analytics.screenshot.impl.config.clientservice.model.ParcelableRemoteScreenshotConfig

class BundleToClientScreenshotConfigConverter : BundleToServiceConfigConverter<ParcelableRemoteScreenshotConfig> {

    private val tag = "[BundleToClientScreenshotConfigConverter]"

    override fun fromBundle(bundle: Bundle): ParcelableRemoteScreenshotConfig {
        DebugLogger.info(tag, "Called fromBundle")
        bundle.classLoader = ParcelableRemoteScreenshotConfig::class.java.classLoader
        return bundle.getParcelable(Constants.ParcelableConfig.CONFIG) ?: ParcelableRemoteScreenshotConfig()
    }
}
