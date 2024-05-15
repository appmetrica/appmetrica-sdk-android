package io.appmetrica.analytics.coreutils.internal.services.frequency

import io.appmetrica.analytics.coreutils.internal.time.SystemTimeProvider
import io.appmetrica.analytics.coreutils.internal.time.TimeProvider
import io.appmetrica.analytics.logger.internal.DebugLogger

class EventFrequencyOverWindowLimitDetector(
    private var window: Long,
    private var limitPerWindow: Int,
    private val storage: EventFrequencyStorage
) {

    private val tag = "[EventFrequencyOverWindowLimitDetector]"

    private val timeProvider: TimeProvider = SystemTimeProvider()

    fun detect(key: String): Boolean {
        val uptime = timeProvider.uptimeMillis()
        val lastWindowStart = storage.getOrPutWindowStart(key, uptime)
        val delta = uptime - lastWindowStart

        DebugLogger.info(
            tag,
            "detect for key: %s; uptime = %s; lastWindowStart = %s; delta = %s; " +
                "windowOccurrencesCountFromStorage before = %s",
            key, uptime, lastWindowStart, delta, storage.getWindowOccurrencesCount(key)
        )

        // Delta < 0 denotes reboot. Ignore it as very rarely event.
        if (delta < 0 || delta > window) {
            storage.putWindowStart(key, uptime)
            storage.putWindowOccurrencesCount(key, 1)
            return false
        }
        val occurrencesCount = (storage.getWindowOccurrencesCount(key) ?: 0) + 1
        storage.putWindowOccurrencesCount(key, occurrencesCount)

        return occurrencesCount > limitPerWindow
    }

    @Synchronized
    fun updateParameters(window: Long, limitPerWindow: Int) {
        this.window = window
        this.limitPerWindow = limitPerWindow
    }

    private fun EventFrequencyStorage.getOrPutWindowStart(key: String, value: Long): Long {
        val actualValue = getWindowStart(key)
        return if (actualValue == null) {
            putWindowStart(key, value)
            value
        } else {
            actualValue
        }
    }
}
