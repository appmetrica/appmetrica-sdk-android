package io.appmetrica.analytics.screenshot.impl.config.service

import io.appmetrica.analytics.screenshot.impl.Constants
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideApiCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideContentObserverCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideServiceCaptorConfig
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ServiceSideScreenshotConfigToBundleConverterTest : CommonTest() {

    private val config = ServiceSideScreenshotConfig(
        enabled = true,
        apiCaptorConfig = ServiceSideApiCaptorConfig(enabled = false),
        serviceCaptorConfig = ServiceSideServiceCaptorConfig(enabled = true, delaySeconds = 43L),
        contentObserverCaptorConfig = ServiceSideContentObserverCaptorConfig(
            enabled = false,
            mediaStoreColumnNames = listOf("first"),
            detectWindowSeconds = 12L
        )
    )

    private val converter = ServiceSideScreenshotConfigToBundleConverter()

    @Test
    fun convert() {
        val bundle = converter.convert(config)!!

        assertThat(bundle.getBoolean(Constants.ServiceConfig.ENABLED)).isTrue()
        assertThat(bundle.getBoolean(Constants.ServiceConfig.API_CAPTOR_ENABLED)).isFalse()
        assertThat(bundle.getBoolean(Constants.ServiceConfig.SERVICE_CAPTOR_ENABLED)).isTrue()
        assertThat(bundle.getLong(Constants.ServiceConfig.SERVICE_CAPTOR_DELAY_SECONDS)).isEqualTo(43L)
        assertThat(bundle.getBoolean(Constants.ServiceConfig.CONTENT_OBSERVER_ENABLED)).isFalse()
        assertThat(bundle.getStringArrayList(Constants.ServiceConfig.CONTENT_OBSERVER_MEDIA_STORE_COLUMN_NAMES))
            .isEqualTo(arrayListOf("first"))
        assertThat(bundle.getLong(Constants.ServiceConfig.CONTENT_OBSERVER_DETECT_WINDOW_SECONDS)).isEqualTo(12L)
    }

    @Test
    fun convertIfConfigIsNull() {
        val bundle = converter.convert(null)

        assertThat(bundle).isNull()
    }
}
