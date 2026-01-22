package io.appmetrica.analytics.screenshot.impl

import android.os.Bundle
import io.appmetrica.analytics.screenshot.impl.config.clientservice.model.ParcelableApiCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.clientservice.model.ParcelableContentObserverCaptorConfig
import io.appmetrica.analytics.screenshot.impl.config.clientservice.model.ParcelableScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.clientservice.model.ParcelableServiceCaptorConfig
import io.appmetrica.analytics.screenshot.internal.config.ParcelableRemoteScreenshotConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class BundleToClientScreenshotConfigConverterTest : CommonTest() {

    private val config = ParcelableRemoteScreenshotConfig(
        enabled = true,
        config = ParcelableScreenshotConfig(
            apiCaptorConfig = ParcelableApiCaptorConfig(
                enabled = false
            ),
            serviceCaptorConfig = ParcelableServiceCaptorConfig(
                enabled = true,
                delaySeconds = 43L
            ),
            contentObserverCaptorConfig = ParcelableContentObserverCaptorConfig(
                enabled = false,
                mediaStoreColumnNames = listOf("first"),
                detectWindowSeconds = 12L
            )
        )
    )

    private val converter = BundleToClientScreenshotConfigConverter()

    @Test
    fun fromBundle() {
        val bundle = Bundle()
        bundle.putParcelable("config", config)

        val result = converter.fromBundle(bundle)
        assertThat(result).usingRecursiveComparison().isEqualTo(config)
    }

    @Test
    fun fromBundleIfNoConfig() {
        val bundle = Bundle()

        val result = converter.fromBundle(bundle)
        assertThat(result).usingRecursiveComparison().isEqualTo(ParcelableRemoteScreenshotConfig())
    }
}
