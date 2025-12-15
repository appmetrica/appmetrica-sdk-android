package io.appmetrica.analytics.network.impl

import io.appmetrica.analytics.networkapi.Call
import io.appmetrica.analytics.networkapi.NetworkClient
import io.appmetrica.analytics.networkapi.NetworkClientSettings
import io.appmetrica.analytics.networkapi.Request

internal class DummyNetworkClient(settings: NetworkClientSettings) : NetworkClient(settings) {

    override fun newCall(request: Request): Call = DummyCall()
}
