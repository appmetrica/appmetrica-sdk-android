package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.modulesapi.internal.network.NetworkClientWithCacheControl
import io.appmetrica.analytics.modulesapi.internal.network.SimpleNetworkApi
import io.appmetrica.analytics.networktasks.internal.CacheControlHttpsConnectionPerformer

internal class SimpleNetworkApiImpl : SimpleNetworkApi {

    private val cacheControlHttpsConnectionPerformer = CacheControlHttpsConnectionPerformer(
        GlobalServiceLocator.getInstance().sslSocketFactoryProvider.sslSocketFactory
    )

    override fun performRequestWithCacheControl(url: String, client: NetworkClientWithCacheControl) {
        cacheControlHttpsConnectionPerformer.performConnection(url, CacheControlConnectionHttpsClientImpl(client))
    }
}
