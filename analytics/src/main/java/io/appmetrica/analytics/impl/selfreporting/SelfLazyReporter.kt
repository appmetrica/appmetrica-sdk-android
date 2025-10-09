package io.appmetrica.analytics.impl.selfreporting

interface SelfLazyReporter {
    fun reportLazyEvent(lazyTask: SelfReportingLazyEventTask)
}
