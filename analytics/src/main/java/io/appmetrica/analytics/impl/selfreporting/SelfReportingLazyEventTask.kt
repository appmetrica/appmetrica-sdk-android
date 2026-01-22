package io.appmetrica.analytics.impl.selfreporting

internal interface SelfReportingLazyEventTask {

    fun get(): SelfReportingLazyEvent?
}
