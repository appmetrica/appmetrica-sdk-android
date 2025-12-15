package io.appmetrica.analytics.networkokhttp.internal

import io.appmetrica.analytics.networkapi.NetworkClient
import io.appmetrica.analytics.networkokhttp.impl.OkHttpNetworkClient

class OkHttpNetworkClientBuilder : NetworkClient.Builder() {

    override fun build(): NetworkClient {
        return OkHttpNetworkClient(settings)
    }

    override fun toString(): String {
        return "OkHttp Network Client Builder"
    }
}
