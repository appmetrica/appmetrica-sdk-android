package io.appmetrica.analytics.coreutils.internal.time

import android.os.SystemClock

class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }

    override fun currentTimeSeconds(): Long {
        return System.currentTimeMillis() / 1000
    }

    override fun elapsedRealtime(): Long {
        return SystemClock.elapsedRealtime()
    }

    override fun systemNanoTime(): Long {
        return System.nanoTime()
    }
}
