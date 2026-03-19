package io.appmetrica.analytics.impl.crash.jvm.service

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.impl.crash.jvm.JvmCrash

internal class FileCrashTimestampProvider : CrashTimestampProvider {
    private val timeProvider = SystemTimeProvider()

    override fun getTimestamp(crash: JvmCrash): Long =
        if (crash.fileModifiedTimestamp > 0) crash.fileModifiedTimestamp
        else timeProvider.currentTimeMillis()
}
