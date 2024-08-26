package io.appmetrica.analytics.modulesapi.internal.service

import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.control.Toggle

abstract class LocationServiceExtension {

    abstract val locationConsumer: Consumer<Location?>?

    abstract val locationSourcesController: ModuleLocationSourcesServiceController?

    abstract val locationControllerAppStateToggle: Toggle?
}
