package io.appmetrica.analytics.impl.events

internal class NativeMainReporterEventSendingPolicy : MainReporterEventSendingPolicy {

    override val condition: EventCondition = EventCondition { true }
}
