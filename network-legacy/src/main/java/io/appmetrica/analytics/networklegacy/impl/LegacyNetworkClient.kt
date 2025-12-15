package io.appmetrica.analytics.networklegacy.impl

import io.appmetrica.analytics.networkapi.Call
import io.appmetrica.analytics.networkapi.NetworkClient
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request

internal class LegacyNetworkClient(
    settings: NetworkClientSettings,
) : NetworkClient(settings) {

    override fun newCall(request: Request): Call {
        return CallImpl(settings, request)
    }

    override fun toString(): String {
        return "LegacyNetworkClient(settings=$settings)"
    }
}
