package io.appmetrica.analytics.modulesapi.internal.service

import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.control.Toggle

interface LocationServiceExtension {

    val locationConsumer: Consumer<Location?>?

    val locationSourcesController: ModuleLocationSourcesServiceController?

    val locationControllerAppStateToggle: Toggle?
}
