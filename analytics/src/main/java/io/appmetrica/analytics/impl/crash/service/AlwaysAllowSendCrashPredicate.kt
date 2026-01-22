package io.appmetrica.analytics.impl.crash.service

internal class AlwaysAllowSendCrashPredicate<T> : ShouldSendCrashNowPredicate<T> {
    override fun shouldSend(crash: T): Boolean = true
}
