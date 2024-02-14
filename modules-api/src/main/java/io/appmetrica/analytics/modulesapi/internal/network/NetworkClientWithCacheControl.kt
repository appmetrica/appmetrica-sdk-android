package io.appmetrica.analytics.modulesapi.internal.network

interface NetworkClientWithCacheControl {

    val eTag: String?

    fun onResponse(eTag: String, response: ByteArray)

    fun onNotModified()

    fun onError()
}
