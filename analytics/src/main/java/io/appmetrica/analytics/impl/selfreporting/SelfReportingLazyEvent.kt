package io.appmetrica.analytics.impl.selfreporting

internal class SelfReportingLazyEvent(val eventName: String, val eventValue: String) {

    override fun toString(): String {
        return "SelfReportingLazyEvent(eventName='$eventName', eventValue='$eventValue')"
    }
}
