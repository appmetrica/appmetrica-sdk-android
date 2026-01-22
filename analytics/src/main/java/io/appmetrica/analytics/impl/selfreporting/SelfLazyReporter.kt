package io.appmetrica.analytics.impl.selfreporting

internal interface SelfLazyReporter {
    fun reportLazyEvent(lazyTask: SelfReportingLazyEventTask)
}
