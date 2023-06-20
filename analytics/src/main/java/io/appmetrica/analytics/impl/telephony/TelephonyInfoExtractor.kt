package io.appmetrica.analytics.impl.telephony

internal interface TelephonyInfoExtractor<T> {
    fun extract(): T
}
