package io.appmetrica.analytics.impl.crash.ndk.service

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.crash.ndk.AppMetricaNativeCrash

internal class CreationTimeNativeCrashTimestampProvider : NativeCrashTimestampProvider {
    private val timeProvider = SystemTimeProvider()

    override fun getTimestamp(crash: AppMetricaNativeCrash): Long =
        if (crash.creationTime > 0) crash.creationTime
        else timeProvider.currentTimeMillis()
}
