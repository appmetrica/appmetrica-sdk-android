package io.appmetrica.analytics.impl.crash.jvm.client

internal fun interface AnrReporter {

    fun reportAnr(allThreads: AllThreads)
}
