package io.appmetrica.analytics.modulesapi.internal.network

interface SimpleNetworkApi {

    fun performRequestWithCacheControl(url: String, client: NetworkClientWithCacheControl)
}
