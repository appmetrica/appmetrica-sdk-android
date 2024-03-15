package io.appmetrica.analytics.coreutils.internal.services.frequency

class InMemoryEventFrequencyStorage : EventFrequencyStorage {

    private val windowStartStorage = mutableMapOf<String, Long>()
    private val windowOccurrencesCountStorage = mutableMapOf<String, Int>()

    override fun putWindowStart(key: String, value: Long) {
        windowStartStorage[key] = value
    }

    override fun getWindowStart(key: String): Long? = windowStartStorage[key]

    override fun putWindowOccurrencesCount(key: String, value: Int) {
        windowOccurrencesCountStorage[key] = value
    }

    override fun getWindowOccurrencesCount(key: String): Int? = windowOccurrencesCountStorage[key]
}
