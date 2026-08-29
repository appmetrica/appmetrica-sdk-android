package io.appmetrica.analytics.impl.component.clients

import io.appmetrica.analytics.impl.CoreServiceEvent
import io.appmetrica.analytics.impl.component.CommonArguments

internal interface ClientUnit {
    fun handle(serviceEvent: CoreServiceEvent, sdkConfig: CommonArguments)

    fun onDisconnect()
}
