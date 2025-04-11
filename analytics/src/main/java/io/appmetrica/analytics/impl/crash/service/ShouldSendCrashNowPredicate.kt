package io.appmetrica.analytics.impl.crash.service

fun interface ShouldSendCrashNowPredicate<T> {

    fun shouldSend(crash: T): Boolean
}
