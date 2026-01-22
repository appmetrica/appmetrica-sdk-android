package io.appmetrica.analytics.impl.events

internal interface MainReporterEventSendingPolicy {

    val condition: EventCondition
}
