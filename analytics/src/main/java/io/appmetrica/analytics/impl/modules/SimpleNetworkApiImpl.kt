package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.impl.GlobalServiceLocator
import io.appmetrica.analytics.modulesapi.internal.network.NetworkClientWithCacheControl
import io.appmetrica.analytics.modulesapi.internal.network.SimpleNetworkApi
import io.appmetrica.analytics.networktasks.internal.CacheControlHttpsConnectionPerformer

internal class SimpleNetworkApiImpl : SimpleNetworkApi {

    override fun performRequestWithCacheControl(url: String, client: NetworkClientWithCacheControl) {
        val sslConfiguration = GlobalServiceLocator.getInstance().sslSocketFactoryProvider.sslConfiguration
        CacheControlHttpsConnectionPerformer(
            sslConfiguration.sslSocketFactory,
            sslConfiguration.trustManager
        ).performConnection(url, CacheControlConnectionHttpsClientImpl(client))
    }
}
