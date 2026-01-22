package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.modulesapi.internal.network.NetworkClientWithCacheControl
import io.appmetrica.analytics.networktasks.internal.CacheControlHttpsConnectionPerformer

internal class CacheControlConnectionHttpsClientImpl(
    private val client: NetworkClientWithCacheControl
) : CacheControlHttpsConnectionPerformer.Client {

    override fun getOldETag(): String? {
        return client.eTag
    }

    override fun onResponse(newETag: String, response: ByteArray) {
        client.onResponse(newETag, response)
    }

    override fun onNotModified() {
        client.onNotModified()
    }

    override fun onError() {
        client.onError()
    }
}
