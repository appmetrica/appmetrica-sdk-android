package io.appmetrica.analytics.coreutils.internal.services.frequency

interface EventFrequencyStorage {

    fun putWindowStart(key: String, value: Long)
    fun getWindowStart(key: String): Long?

    fun putWindowOccurrencesCount(key: String, value: Int)
    fun getWindowOccurrencesCount(key: String): Int?
}
