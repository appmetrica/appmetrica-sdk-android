package io.appmetrica.analytics.network.impl

import io.appmetrica.analytics.networkapi.NetworkClient

internal class DummyNetworkClientBuilder : NetworkClient.Builder() {

    override fun build() = DummyNetworkClient(settings)

    override fun toString(): String {
        return "Dummy Network Client Builder"
    }
}
