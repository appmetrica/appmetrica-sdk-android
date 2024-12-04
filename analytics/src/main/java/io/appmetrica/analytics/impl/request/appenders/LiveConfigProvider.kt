package io.appmetrica.analytics.impl.request.appenders

import io.appmetrica.analytics.coreapi.internal.identifiers.AdvertisingIdsHolder
import io.appmetrica.analytics.impl.GlobalServiceLocator

class LiveConfigProvider {

    val advertisingIdentifiers: AdvertisingIdsHolder
        get() = GlobalServiceLocator.getInstance().advertisingIdGetter.identifiers
}
