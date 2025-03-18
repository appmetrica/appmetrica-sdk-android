package io.appmetrica.analytics.screenshot.impl

import io.appmetrica.analytics.screenshot.impl.config.clientservice.model.ParcelableRemoteScreenshotConfig
import io.appmetrica.analytics.screenshot.impl.config.service.model.ServiceSideRemoteScreenshotConfig
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ServiceToBundleScreenshotConfigConverterTest : CommonTest() {

    private val config = ServiceSideRemoteScreenshotConfig()

    private val converter = ServiceToBundleScreenshotConfigConverter()

    @Test
    fun convert() {
        val bundle = converter.convert(config)

        assertThat(bundle?.containsKey("config")).isTrue()

        bundle?.classLoader = ParcelableRemoteScreenshotConfig::class.java.classLoader
        val parcelableConfig = bundle?.getParcelable<ParcelableRemoteScreenshotConfig>("config")

        assertThat(parcelableConfig).isNotNull()
        assertThat(parcelableConfig).usingRecursiveComparison().isEqualTo(ParcelableRemoteScreenshotConfig(config))
    }

    @Test
    fun convertIfConfigIsNull() {
        val bundle = converter.convert(null)

        assertThat(bundle).isNull()
    }
}
