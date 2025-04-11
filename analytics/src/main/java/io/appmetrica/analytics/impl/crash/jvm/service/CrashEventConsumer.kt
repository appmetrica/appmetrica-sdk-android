package io.appmetrica.analytics.impl.crash.jvm.service

import io.appmetrica.analytics.impl.CounterReport
import io.appmetrica.analytics.impl.component.CommonArguments
import io.appmetrica.analytics.impl.component.clients.ClientDescription

internal interface CrashEventConsumer {
    fun consumeCrash(
        clientDescription: ClientDescription,
        counterReport: CounterReport,
        commonArguments: CommonArguments
    )
}
