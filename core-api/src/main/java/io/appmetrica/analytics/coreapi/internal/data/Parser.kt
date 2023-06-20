package io.appmetrica.analytics.coreapi.internal.data

interface Parser<in IN, out OUT : Any> {
    fun parse(rawData: IN): OUT
    fun parseOrNull(rawData: IN) = runCatching { parse(rawData) }.getOrNull()
}
