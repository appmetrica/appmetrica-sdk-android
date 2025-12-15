package io.appmetrica.analytics.network.impl

import io.appmetrica.analytics.networkapi.Call
import io.appmetrica.analytics.networkapi.Response

internal class DummyCall : Call() {

    override fun execute() = Response.Builder(IllegalStateException("This is dummy call")).build()
}
