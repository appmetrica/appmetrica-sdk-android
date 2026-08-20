package io.appmetrica.analytics.impl.component.clients

import io.appmetrica.analytics.impl.ServiceEvent
import io.appmetrica.analytics.impl.component.CommonArguments

internal interface ClientUnit {
    fun handle(serviceEvent: ServiceEvent, sdkConfig: CommonArguments)

    fun onDisconnect()
}
