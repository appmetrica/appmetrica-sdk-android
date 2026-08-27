package io.appmetrica.analytics.adrevenue.other.impl.config.service

import io.appmetrica.analytics.adrevenue.other.impl.Constants
import io.appmetrica.analytics.adrevenue.other.impl.config.service.model.ServiceSideAdRevenueOtherConfig
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ServiceSideAdRevenueOtherConfigToBundleConverterTest : CommonTest() {

    private val config = ServiceSideAdRevenueOtherConfig(
        enabled = true,
        includeSource = true,
    )

    private val converter = ServiceSideAdRevenueOtherConfigToBundleConverter()

    @Test
    fun convert() {
        val bundle = converter.convert(config)!!

        assertThat(bundle.getBoolean(Constants.ServiceConfig.ENABLED)).isTrue()
        assertThat(bundle.getBoolean(Constants.ServiceConfig.INCLUDE_SOURCE)).isTrue()
    }

    @Test
    fun convertWithFalseValues() {
        val falseConfig = ServiceSideAdRevenueOtherConfig(
            enabled = false,
            includeSource = false,
        )
        val bundle = converter.convert(falseConfig)!!

        assertThat(bundle.getBoolean(Constants.ServiceConfig.ENABLED)).isFalse()
        assertThat(bundle.getBoolean(Constants.ServiceConfig.INCLUDE_SOURCE)).isFalse()
    }

    @Test
    fun convertIfConfigIsNull() {
        assertThat(converter.convert(null)).isNull()
    }
}
