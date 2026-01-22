package io.appmetrica.analytics.impl.adrevenue

import android.content.Context
import io.appmetrica.analytics.impl.ClientServiceLocator

internal class AdRevenueSupportedSourcesPluginProvider(
    private val context: Context
) : AdRevenueSupportedSourcesProvider {

    override val metaInfo: String?
        get() = ClientServiceLocator.getInstance().getExtraMetaInfoRetriever(context).pluginAdRevenueMetaInfoSources
}
