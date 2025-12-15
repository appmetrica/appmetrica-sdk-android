package io.appmetrica.analytics.network.internal

import io.appmetrica.analytics.network.impl.NetworkClientFactory
import io.appmetrica.analytics.networkapi.NetworkClient

class NetworkClientBuilder : NetworkClient.Builder() {

    override fun build(): NetworkClient {
        return NetworkClientFactory.createNetworkClient(settings)
    }
}
