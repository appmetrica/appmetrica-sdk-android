package io.appmetrica.analytics.impl.id

interface AdvertisingStateFromClientObserver {

    fun setInitialStateFromClientConfigIfNotDefined(enabled: Boolean)

    fun updateStateFromClientConfig(enabled: Boolean)
}
