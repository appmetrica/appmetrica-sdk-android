package io.appmetrica.analytics.impl.crash.service

internal fun interface ShouldSendCrashNowPredicate<T> {

    fun shouldSend(crash: T): Boolean
}
