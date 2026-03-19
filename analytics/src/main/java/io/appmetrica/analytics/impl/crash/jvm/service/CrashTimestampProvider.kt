package io.appmetrica.analytics.impl.crash.jvm.service

import io.appmetrica.analytics.impl.crash.jvm.JvmCrash

internal fun interface CrashTimestampProvider {
    fun getTimestamp(crash: JvmCrash): Long
}
