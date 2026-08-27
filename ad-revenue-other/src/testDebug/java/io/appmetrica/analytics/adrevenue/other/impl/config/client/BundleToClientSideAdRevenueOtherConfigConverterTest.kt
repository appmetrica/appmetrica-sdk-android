package io.appmetrica.analytics.adrevenue.other.impl.config.client

import android.os.Bundle
import io.appmetrica.analytics.adrevenue.other.impl.Constants
import io.appmetrica.analytics.adrevenue.other.impl.config.client.model.ClientSideAdRevenueOtherConfig
import io.appmetrica.analytics.adrevenue.other.internal.ClientSideAdRevenueOtherConfigWrapper
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class BundleToClientSideAdRevenueOtherConfigConverterTest : CommonTest() {

    private val converter = BundleToClientSideAdRevenueOtherConfigConverter()

    @Test
    fun fromBundle() {
        val bundle = Bundle().apply {
            putBoolean(Constants.ServiceConfig.ENABLED, true)
            putBoolean(Constants.ServiceConfig.INCLUDE_SOURCE, true)
        }

        val result = converter.fromBundle(bundle)

        val expected = ClientSideAdRevenueOtherConfigWrapper(
            ClientSideAdRevenueOtherConfig(enabled = true, includeSource = true)
        )
        assertThat(result).usingRecursiveComparison().isEqualTo(expected)
    }

    @Test
    fun fromBundleIfNoConfig() {
        val result = converter.fromBundle(Bundle())

        val expected = ClientSideAdRevenueOtherConfigWrapper(
            ClientSideAdRevenueOtherConfig(
                enabled = Constants.Defaults.DEFAULT_ENABLED,
                includeSource = Constants.Defaults.DEFAULT_INCLUDE_SOURCE,
            )
        )
        assertThat(result).usingRecursiveComparison().isEqualTo(expected)
    }
}
