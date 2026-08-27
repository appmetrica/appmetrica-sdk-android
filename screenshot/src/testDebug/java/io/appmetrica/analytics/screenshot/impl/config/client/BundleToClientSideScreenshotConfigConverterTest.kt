package io.appmetrica.analytics.screenshot.impl.config.client

import android.os.Bundle
import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideApiCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideContentObserverCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.client.model.ClientSideServiceCaptorConfig
import io.appmetrica.analytics.screenshot.internal.ClientSideScreenshotConfigWrapper
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class BundleToClientSideScreenshotConfigConverterTest : CommonTest() {

    private val converter = BundleToClientSideScreenshotConfigConverter()

    @Test
    fun fromBundle() {
        val bundle = Bundle()
        bundle.putBoolean(Constants.ServiceConfig.ENABLED, true)
        bundle.putBoolean(Constants.ServiceConfig.API_CAPTOR_ENABLED, false)
        bundle.putBoolean(Constants.ServiceConfig.SERVICE_CAPTOR_ENABLED, true)
        bundle.putLong(Constants.ServiceConfig.SERVICE_CAPTOR_DELAY_SECONDS, 43L)
        bundle.putBoolean(Constants.ServiceConfig.CONTENT_OBSERVER_ENABLED, false)
        bundle.putStringArrayList(
            Constants.ServiceConfig.CONTENT_OBSERVER_MEDIA_STORE_COLUMN_NAMES,
            arrayListOf("first")
        )
        bundle.putLong(Constants.ServiceConfig.CONTENT_OBSERVER_DETECT_WINDOW_SECONDS, 12L)

        val result = converter.fromBundle(bundle)

        val expected = ClientSideScreenshotConfigWrapper(
            ClientSideScreenshotConfig(
                enabled = true,
                apiCaptorConfig = ClientSideApiCaptorConfig(enabled = false),
                serviceCaptorConfig = ClientSideServiceCaptorConfig(enabled = true, delaySeconds = 43L),
                contentObserverCaptorConfig = ClientSideContentObserverCaptorConfig(
                    enabled = false,
                    mediaStoreColumnNames = listOf("first"),
                    detectWindowSeconds = 12L
                )
            )
        )
        assertThat(result).usingRecursiveComparison().isEqualTo(expected)
    }

    @Test
    fun fromBundleIfNoConfig() {
        val bundle = Bundle()

        val result = converter.fromBundle(bundle)

        val expected = ClientSideScreenshotConfigWrapper(
            ClientSideScreenshotConfig(
                enabled = Constants.Defaults.DEFAULT_FEATURE_STATE,
                apiCaptorConfig = ClientSideApiCaptorConfig(enabled = Constants.Defaults.DEFAULT_API_CAPTOR_ENABLED),
                serviceCaptorConfig = ClientSideServiceCaptorConfig(
                    enabled = Constants.Defaults.DEFAULT_SERVICE_CAPTOR_ENABLED,
                    delaySeconds = Constants.Defaults.DEFAULT_SERVICE_CAPTOR_DELAY_SECONDS,
                ),
                contentObserverCaptorConfig = ClientSideContentObserverCaptorConfig(
                    enabled = Constants.Defaults.DEFAULT_CONTENT_OBSERVER_ENABLED,
                    mediaStoreColumnNames = Constants.Defaults.defaultMediaStoreColumnNames.toList(),
                    detectWindowSeconds = Constants.Defaults.DEFAULT_CONTENT_OBSERVER_DETECT_WINDOW_SECONDS,
                )
            )
        )
        assertThat(result).usingRecursiveComparison().isEqualTo(expected)
    }
}
