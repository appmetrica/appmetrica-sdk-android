package io.appmetrica.analytics.impl.selfreporting

interface SelfReportingLazyEventTask {

    fun get(): SelfReportingLazyEvent?
}
