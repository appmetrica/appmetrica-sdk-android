package io.appmetrica.analytics.impl.component.clients

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.CommonArguments

internal interface ClientUnit {
    fun handle(report: CounterReport, sdkConfig: CommonArguments)

    fun onDisconnect()
}
