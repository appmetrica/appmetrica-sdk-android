package io.appmetrica.analytics.impl.events

interface MainReporterEventSendingPolicy {

    val condition: EventCondition
}
