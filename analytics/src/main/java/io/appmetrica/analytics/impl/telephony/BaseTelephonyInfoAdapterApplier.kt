package io.appmetrica.analytics.impl.telephony

internal class BaseTelephonyInfoAdapterApplier<T>(
    private val extractor: TelephonyInfoExtractor<T>
) : TelephonyInfoAdapterApplier<T> {

    override fun applyAdapter(adapter: TelephonyInfoAdapter<T>) {
        adapter.adopt(extractor.extract())
    }
}
