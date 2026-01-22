package io.appmetrica.analytics.impl.id

internal interface AdvertisingStateFromClientObserver {

    fun setInitialStateFromClientConfigIfNotDefined(enabled: Boolean)

    fun updateStateFromClientConfig(enabled: Boolean)
}
