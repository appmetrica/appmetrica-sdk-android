package io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.client

import android.os.Bundle
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.Constants
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.impl.config.client.model.ClientApplovinConfig
import io.appmetrica.analytics.adrevenue.applovin.v12.auto.internal.ClientApplovinConfigWrapper
import io.appmetrica.gradle.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

internal class BundleToClientApplovinConfigConverterTest : CommonTest() {

    private val converter = BundleToClientApplovinConfigConverter()

    @Test
    fun fromBundleEnabled() {
        val bundle: Bundle = mock {
            on { getBoolean(Constants.ServiceConfig.ENABLED, Constants.Defaults.DEFAULT_ENABLED) } doReturn true
        }

        val result = converter.fromBundle(bundle)

        val expected = ClientApplovinConfigWrapper(
            ClientApplovinConfig(enabled = true)
        )
        assertThat(result).usingRecursiveComparison().isEqualTo(expected)
    }

    @Test
    fun fromBundleDisabled() {
        val bundle: Bundle = mock {
            on { getBoolean(Constants.ServiceConfig.ENABLED, Constants.Defaults.DEFAULT_ENABLED) } doReturn false
        }

        val result = converter.fromBundle(bundle)

        val expected = ClientApplovinConfigWrapper(
            ClientApplovinConfig(enabled = false)
        )
        assertThat(result).usingRecursiveComparison().isEqualTo(expected)
    }

    @Test
    fun fromEmptyBundleUsesDefault() {
        val bundle: Bundle = mock {
            on {
                getBoolean(Constants.ServiceConfig.ENABLED, Constants.Defaults.DEFAULT_ENABLED)
            } doReturn Constants.Defaults.DEFAULT_ENABLED
        }

        val result = converter.fromBundle(bundle)

        val expected = ClientApplovinConfigWrapper(
            ClientApplovinConfig(enabled = Constants.Defaults.DEFAULT_ENABLED)
        )
        assertThat(result).usingRecursiveComparison().isEqualTo(expected)
    }
}
