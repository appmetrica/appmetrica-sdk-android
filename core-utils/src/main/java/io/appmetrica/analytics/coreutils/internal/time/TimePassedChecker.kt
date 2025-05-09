package io.appmetrica.analytics.coreutils.internal.time

import androidx.annotation.VisibleForTesting
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger

class TimePassedChecker @VisibleForTesting internal constructor(private val mTimeProvider: TimeProvider) {
    constructor() : this(SystemTimeProvider())

    fun didTimePassSeconds(lastTimeSeconds: Long, intervalSeconds: Long, tag: String): Boolean {
        return didTimePass(mTimeProvider.currentTimeSeconds(), lastTimeSeconds, intervalSeconds, tag)
    }

    fun didTimePassMillis(lastTimeMillis: Long, intervalMillis: Long, tag: String): Boolean {
        return didTimePass(mTimeProvider.currentTimeMillis(), lastTimeMillis, intervalMillis, tag)
    }

    private fun didTimePass(currentTime: Long, lastTime: Long, interval: Long, tag: String): Boolean {
        DebugLogger.info(
            tag,
            "didTimePass?: ${currentTime - lastTime >= interval} current time: $currentTime, " +
                "last time: $lastTime, interval $interval."
        )
        if (currentTime < lastTime) {
            DebugLogger.warning(tag, "current time is less than last.")
            return true
        }
        return currentTime - lastTime >= interval
    }
}
