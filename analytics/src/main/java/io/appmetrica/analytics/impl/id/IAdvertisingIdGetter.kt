package io.appmetrica.analytics.impl.id

import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsProvider
import io.appmetrica.analytics.impl.StartupStateObserver

internal interface IAdvertisingIdGetter :
    AdvertisingIdsProvider, AdvertisingStateFromClientObserver, StartupStateObserver {

    fun init()
}
