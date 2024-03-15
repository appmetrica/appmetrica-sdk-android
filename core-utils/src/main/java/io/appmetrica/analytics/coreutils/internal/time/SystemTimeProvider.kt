package io.appmetrica.analytics.coreutils.internal.time

import android.os.SystemClock

class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()

    override fun currentTimeSeconds(): Long = System.currentTimeMillis() / 1000

    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()

    override fun systemNanoTime(): Long = System.nanoTime()

    override fun uptimeMillis(): Long = SystemClock.uptimeMillis()
}
