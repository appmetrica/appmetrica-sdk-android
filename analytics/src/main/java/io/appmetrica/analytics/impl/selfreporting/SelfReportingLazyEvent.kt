package io.appmetrica.analytics.impl.selfreporting

class SelfReportingLazyEvent(val eventName: String, val eventValue: String) {

    override fun toString(): String {
        return "SelfReportingLazyEvent(eventName='$eventName', eventValue='$eventValue')"
    }
}
