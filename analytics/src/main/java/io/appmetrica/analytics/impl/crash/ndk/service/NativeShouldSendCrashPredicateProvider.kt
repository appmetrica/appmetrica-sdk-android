package io.appmetrica.analytics.impl.crash.ndk.service

import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash
import io.appmetrica.analytics.impl.crash.service.ShouldSendCrashNowPredicate

fun interface NativeShouldSendCrashPredicateProvider {
    fun predicate(nativeCrash: AppMetricaNativeCrash): ShouldSendCrashNowPredicate<String>
}
