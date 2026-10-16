package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.modulesapi.internal.network.NetworkClientWithCacheControl
import io.appmetrica.analytics.modulesapi.internal.network.SimpleNetworkApi
import io.appmetrica.analytics.networktasks.internal.CacheControlHttpsConnectionPerformer

internal class SimpleNetworkApiImpl : SimpleNetworkApi {

    override fun performRequestWithCacheControl(url: String, client: NetworkClientWithCacheControl) {
        val provider = GlobalServiceLocator.getInstance().sslSocketFactoryProvider
        CacheControlHttpsConnectionPerformer(
            provider.sslSocketFactory,
            provider.trustManager
        ).performConnection(url, CacheControlConnectionHttpsClientImpl(client))
    }
}
