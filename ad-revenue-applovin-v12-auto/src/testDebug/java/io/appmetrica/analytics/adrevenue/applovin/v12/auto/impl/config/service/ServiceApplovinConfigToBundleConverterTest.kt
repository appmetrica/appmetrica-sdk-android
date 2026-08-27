package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service

import android.annotation.SuppressLint
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.Constants
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.service.model.ServiceApplovinConfig
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@SuppressLint("RobolectricUsage") // Bundle usage
@RunWith(RobolectricTestRunner::class)
internal class ServiceApplovinConfigToBundleConverterTest : CommonTest() {

    private val converter = ServiceApplovinConfigToBundleConverter()

    @Test
    fun convertNullConfigReturnsNull() {
        assertThat(converter.convert(null)).isNull()
    }

    @Test
    fun convertEnabledConfig() {
        val bundle = converter.convert(ServiceApplovinConfig(enabled = true))!!

        assertThat(bundle.getBoolean(Constants.ServiceConfig.ENABLED)).isTrue()
    }

    @Test
    fun convertDisabledConfig() {
        val bundle = converter.convert(ServiceApplovinConfig(enabled = false))!!

        assertThat(bundle.getBoolean(Constants.ServiceConfig.ENABLED)).isFalse()
    }
}
