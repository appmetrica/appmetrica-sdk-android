package io.appmetrica.analytics.impl.telephony

internal interface TelephonyInfoAdapterApplier<T> {

    fun applyAdapter(adapter: TelephonyInfoAdapter<T>)
}
