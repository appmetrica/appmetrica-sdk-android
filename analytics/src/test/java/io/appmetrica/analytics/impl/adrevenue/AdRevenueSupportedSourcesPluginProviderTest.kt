package io.appmetrica.analytics.impl.adrevenue

import android.content.Context
import io.appmetrica.analytics.impl.ClientServiceLocator
import io.appmetrica.analytics.testutils.ClientServiceLocatorRule
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class AdRevenueSupportedSourcesPluginProviderTest : CommonTest() {

    private val context: Context = mock()

    @get:Rule
    val clientServiceLocatorRule = ClientServiceLocatorRule()

    private val provider by setUp { AdRevenueSupportedSourcesPluginProvider(context) }

    @Test
    fun metaInfo() {
        val pluginSources = "plugin sources"

        whenever(ClientServiceLocator.getInstance().getExtraMetaInfoRetriever(context).pluginAdRevenueMetaInfoSources)
            .thenReturn(pluginSources)

        assertThat(provider.metaInfo).isEqualTo(pluginSources)
    }
}
