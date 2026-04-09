package io.appmetrica.analytics.screenshot.impl.config.service

import android.os.Bundle
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig

internal class ServiceSideScreenshotConfigToBundleConverter {

    private val tag = "[ServiceSideScreenshotConfigToBundleConverter]"

    fun convert(config: ServiceSideScreenshotConfig?): Bundle? {
        DebugLogger.info(tag, "convert $config")
        if (config == null) {
            return null
        }
        val bundle = Bundle()
        bundle.putBoolean(Constants.ServiceConfig.ENABLED, config.enabled)
        config.apiCaptorConfig?.let {
            bundle.putBoolean(Constants.ServiceConfig.API_CAPTOR_ENABLED, it.enabled)
        }
        config.serviceCaptorConfig?.let {
            bundle.putBoolean(Constants.ServiceConfig.SERVICE_CAPTOR_ENABLED, it.enabled)
            bundle.putLong(Constants.ServiceConfig.SERVICE_CAPTOR_DELAY_SECONDS, it.delaySeconds)
        }
        config.contentObserverCaptorConfig?.let {
            bundle.putBoolean(Constants.ServiceConfig.CONTENT_OBSERVER_ENABLED, it.enabled)
            bundle.putStringArrayList(
                Constants.ServiceConfig.CONTENT_OBSERVER_MEDIA_STORE_COLUMN_NAMES,
                ArrayList(it.mediaStoreColumnNames)
            )
            bundle.putLong(Constants.ServiceConfig.CONTENT_OBSERVER_DETECT_WINDOW_SECONDS, it.detectWindowSeconds)
        }
        return bundle
    }
}
