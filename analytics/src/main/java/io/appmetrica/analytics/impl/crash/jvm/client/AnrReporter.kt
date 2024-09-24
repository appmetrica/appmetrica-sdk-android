package io.appmetrica.analytics.impl.crash.jvm.client

fun interface AnrReporter {

    fun reportAnr(allThreads: AllThreads)
}
