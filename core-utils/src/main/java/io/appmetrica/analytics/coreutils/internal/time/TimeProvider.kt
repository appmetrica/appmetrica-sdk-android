package io.appmetrica.analytics.coreutils.internal.time

interface TimeProvider {
    fun currentTimeMillis(): Long
    fun currentTimeSeconds(): Long
    fun elapsedRealtime(): Long
    fun systemNanoTime(): Long
    fun uptimeMillis(): Long
}
