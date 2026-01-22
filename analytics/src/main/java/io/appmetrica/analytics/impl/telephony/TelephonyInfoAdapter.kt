package io.appmetrica.analytics.impl.telephony

internal interface TelephonyInfoAdapter<T> {

    fun adopt(value: T)
}
