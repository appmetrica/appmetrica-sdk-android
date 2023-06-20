package io.appmetrica.analytics.coreutils.internal.time

import androidx.annotation.VisibleForTesting
import java.util.concurrent.TimeUnit

class SystemTimeOffsetProvider @VisibleForTesting constructor(private val mSystemTimeProvider: SystemTimeProvider) {
    constructor() : this(SystemTimeProvider())

    fun elapsedRealtimeOffsetInSeconds(timestamp: Long, timeUnit: TimeUnit): Long {
        return TimeUnit.MILLISECONDS.toSeconds(elapsedRealtimeOffset(timestamp, timeUnit))
    }

    fun elapsedRealtimeOffset(timestampInUnit: Long, timeUnit: TimeUnit): Long {
        val timestamp = timeUnit.toMillis(timestampInUnit)
        return mSystemTimeProvider.elapsedRealtime() - timestamp
    }

    fun systemNanoTimeOffsetInNanos(timestamp: Long, timeUnit: TimeUnit): Long {
        return mSystemTimeProvider.systemNanoTime() - timeUnit.toNanos(timestamp)
    }

    fun systemNanoTimeOffsetInSeconds(timestamp: Long, timeUnit: TimeUnit): Long {
        return TimeUnit.NANOSECONDS.toSeconds(systemNanoTimeOffsetInNanos(timestamp, timeUnit))
    }

    fun offsetInSecondsIfNotZero(timestamp: Long, timeUnit: TimeUnit): Long {
        return if (timestamp == 0L) 0 else mSystemTimeProvider.currentTimeSeconds() - timeUnit.toSeconds(timestamp)
    }
}
