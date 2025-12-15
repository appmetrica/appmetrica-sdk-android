package io.appmetrica.analytics.networklegacy.internal

import io.appmetrica.analytics.networkapi.NetworkClient
import io.appmetrica.analytics.networklegacy.impl.LegacyNetworkClient

class LegacyNetworkClientBuilder : NetworkClient.Builder() {

    override fun build(): NetworkClient {
        return LegacyNetworkClient(settings)
    }

    override fun toString(): String {
        return "Legacy Network Client Builder"
    }
}
