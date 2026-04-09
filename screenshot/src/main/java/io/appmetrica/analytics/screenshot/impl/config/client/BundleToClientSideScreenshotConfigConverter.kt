package io.appmetrica.analytics.screenshot.impl.config.client

import android.os.Bundle
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger
import io.appmetrica.analytics.modulesapi.internal.client.BundleToServiceConfigConverter
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideApiCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideContentObserverCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideServiceCaptorConfig
import io.appmetrica.analytics.screenshot.internal.ClientSideScreenshotConfigWrapper
import io.appmetrica.analytics.screenshot.internal.ClientSideScreenshotConfigWrapper.Companion.toWrapper

internal class BundleToClientSideScreenshotConfigConverter :
    BundleToServiceConfigConverter<ClientSideScreenshotConfigWrapper> {

    private val tag = "[BundleToClientSideScreenshotConfigConverter]"

    override fun fromBundle(bundle: Bundle): ClientSideScreenshotConfigWrapper {
        DebugLogger.info(tag, "Called fromBundle")
        val enabled = bundle.getBoolean(Constants.ServiceConfig.ENABLED, Constants.Defaults.DEFAULT_FEATURE_STATE)

        val apiCaptorConfig = ClientSideApiCaptorConfig(
            enabled = bundle.getBoolean(
                Constants.ServiceConfig.API_CAPTOR_ENABLED,
                Constants.Defaults.DEFAULT_API_CAPTOR_ENABLED
            )
        )

        val serviceCaptorConfig = ClientSideServiceCaptorConfig(
            enabled = bundle.getBoolean(
                Constants.ServiceConfig.SERVICE_CAPTOR_ENABLED,
                Constants.Defaults.DEFAULT_SERVICE_CAPTOR_ENABLED
            ),
            delaySeconds = bundle.getLong(
                Constants.ServiceConfig.SERVICE_CAPTOR_DELAY_SECONDS,
                Constants.Defaults.DEFAULT_SERVICE_CAPTOR_DELAY_SECONDS
            )
        )

        val contentObserverCaptorConfig = ClientSideContentObserverCaptorConfig(
            enabled = bundle.getBoolean(
                Constants.ServiceConfig.CONTENT_OBSERVER_ENABLED,
                Constants.Defaults.DEFAULT_CONTENT_OBSERVER_ENABLED
            ),
            mediaStoreColumnNames = bundle.getStringArrayList(
                Constants.ServiceConfig.CONTENT_OBSERVER_MEDIA_STORE_COLUMN_NAMES
            ) ?: Constants.Defaults.defaultMediaStoreColumnNames,
            detectWindowSeconds = bundle.getLong(
                Constants.ServiceConfig.CONTENT_OBSERVER_DETECT_WINDOW_SECONDS,
                Constants.Defaults.DEFAULT_CONTENT_OBSERVER_DETECT_WINDOW_SECONDS
            )
        )

        val screenshotConfig = ClientSideScreenshotConfig(
            enabled = enabled,
            apiCaptorConfig = apiCaptorConfig,
            serviceCaptorConfig = serviceCaptorConfig,
            contentObserverCaptorConfig = contentObserverCaptorConfig,
        )
        return screenshotConfig.toWrapper()
    }
}
