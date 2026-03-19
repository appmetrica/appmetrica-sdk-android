package io.appmetrica.analytics.impl.crash.ndk.service

import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash

internal fun interface NativeCrashTimestampProvider {
    fun getTimestamp(crash: AppMetricaNativeCrash): Long
}
