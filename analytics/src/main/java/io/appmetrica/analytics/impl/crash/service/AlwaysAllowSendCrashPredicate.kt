package io.appmetrica.analytics.impl.crash.service

class AlwaysAllowSendCrashPredicate<T> : ShouldSendCrashNowPredicate<T> {
    override fun shouldSend(crash: T): Boolean = true
}
