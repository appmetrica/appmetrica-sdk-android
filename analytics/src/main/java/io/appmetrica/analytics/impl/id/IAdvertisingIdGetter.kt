package io.appmetrica.analytics.impl.id

import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder
import io.appmetrica.analytics.coreapi.internal.identifiers.SimpleAdvertisingIdGetter
import io.appmetrica.analytics.impl.StartupStateObserver

interface IAdvertisingIdGetter : SimpleAdvertisingIdGetter, StartupStateObserver {

    fun init()

    val identifiers: AdvertisingIdsHolder

    val identifiersForced: AdvertisingIdsHolder

    fun setInitialStateFromClientConfigIfNotDefined(enabled: Boolean)

    fun updateStateFromClientConfig(enabled: Boolean)

    fun getIdentifiersForced(yandexRetryStrategy: RetryStrategy): AdvertisingIdsHolder
}
