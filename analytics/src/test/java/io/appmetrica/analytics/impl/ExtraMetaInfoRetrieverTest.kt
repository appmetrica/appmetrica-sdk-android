package io.appmetrica.analytics.impl

import android.content.Context
import android.os.Bundle
import io.appmetrica.analytics.coreutils.internal.services.SafePackageManager
import io.appmetrica.analytics.testutils.CommonTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ExtraMetaInfoRetrieverTest : CommonTest() {

    private val bundle: Bundle = mock()
    private val context: Context = mock()
    private val packageManager: SafePackageManager = mock {
        on { getApplicationMetaData(context) } doReturn bundle
    }
    private val stringResourceRetriever: StringResourceRetriever = mock()
    private val booleanResourceRetriever: BooleanResourceRetriever = mock()

    private val pluginIdKey = "io.appmetrica.analytics.plugin_id"
    private val pluginAdRevenueMetaInfoSourcesApiKey = "io.appmetrica.analytics.plugin_supported_ad_revenue_sources"

    private val extraMetaInfoRetriever: ExtraMetaInfoRetriever by setUp {
        ExtraMetaInfoRetriever(context, stringResourceRetriever, booleanResourceRetriever, packageManager)
    }

    @Test
    fun noResources() {
        whenever(stringResourceRetriever.resource).thenReturn(null)
        whenever(booleanResourceRetriever.resource).thenReturn(null)
        assertThat(extraMetaInfoRetriever.buildId).isNull()
        assertThat(extraMetaInfoRetriever.isOffline).isNull()
    }

    @Test
    fun hasResources() {
        val buildId = "1234567890"
        val isOffline = false
        whenever(stringResourceRetriever.resource).thenReturn(buildId)
        whenever(booleanResourceRetriever.resource).thenReturn(isOffline)
        assertThat(extraMetaInfoRetriever.buildId).isEqualTo(buildId)
        assertThat(extraMetaInfoRetriever.isOffline).isEqualTo(isOffline)
    }

    @Test
    fun `pluginId for null meta data`() {
        whenever(packageManager.getApplicationMetaData(context)).thenReturn(null)
        assertThat(extraMetaInfoRetriever.pluginId).isNull()
    }

    @Test
    fun `pluginId for empty meta data`() {
        whenever(packageManager.getApplicationMetaData(context)).thenReturn(Bundle())
        assertThat(extraMetaInfoRetriever.pluginId).isNull()
    }

    @Test
    fun `pluginId for empty value`() {
        whenever(bundle.getString(pluginIdKey)).thenReturn("")
        whenever(packageManager.getApplicationMetaData(context)).thenReturn(bundle)
        assertThat(extraMetaInfoRetriever.pluginId).isEmpty()
    }

    @Test
    fun `pluginId for valid value`() {
        val pluginId = "Some plugin id"
        whenever(bundle.getString(pluginIdKey)).thenReturn(pluginId)
        whenever(packageManager.getApplicationMetaData(context)).thenReturn(bundle)
        assertThat(extraMetaInfoRetriever.pluginId).isEqualTo(pluginId)
    }

    @Test
    fun `pluginAdRevenueMetaInfoSources for null meta data`() {
        whenever(packageManager.getApplicationMetaData(context)).thenReturn(null)
        assertThat(extraMetaInfoRetriever.pluginAdRevenueMetaInfoSources).isNull()
    }

    @Test
    fun `pluginAdRevenueMetaInfoSources for empty meta data`() {
        whenever(packageManager.getApplicationMetaData(context)).thenReturn(Bundle())
        assertThat(extraMetaInfoRetriever.pluginAdRevenueMetaInfoSources).isNull()
    }

    @Test
    fun `pluginAdRevenueMetaInfoSources for empty value`() {
        whenever(bundle.getString(pluginAdRevenueMetaInfoSourcesApiKey)).thenReturn("")
        whenever(packageManager.getApplicationMetaData(context)).thenReturn(bundle)
    }

    @Test
    fun `pluginAdRevenueMetaInfoSources for valid value`() {
        val pluginAdRevenueMetaInfoSources = "Some plugin ad revenue meta info sources"
        whenever(bundle.getString(pluginAdRevenueMetaInfoSourcesApiKey)).thenReturn(pluginAdRevenueMetaInfoSources)
        whenever(packageManager.getApplicationMetaData(context)).thenReturn(bundle)
        assertThat(extraMetaInfoRetriever.pluginAdRevenueMetaInfoSources).isEqualTo(pluginAdRevenueMetaInfoSources)
    }
}
