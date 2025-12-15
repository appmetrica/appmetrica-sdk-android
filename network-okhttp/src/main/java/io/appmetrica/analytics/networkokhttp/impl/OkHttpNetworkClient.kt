package io.appmetrica.analytics.networkokhttp.impl

import io.appmetrica.analytics.networkapi.Call
import io.appmetrica.analytics.networkapi.NetworkClient
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request
import okhttp3.OkHttpClient

internal class OkHttpNetworkClient @JvmOverloads constructor(
    settings: NetworkClientSettings,
    private val okHttpClient: OkHttpClient = OkHttpClientFactory().createOkHttpClient(settings),
) : NetworkClient(settings) {

    override fun newCall(request: Request): Call {
        return CallImpl(okHttpClient, request, settings)
    }

    override fun toString(): String {
        return "OkHttpNetworkClient(settings=$settings)"
    }
}
