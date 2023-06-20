package io.appmetrica.analytics.modulesapi.internal

import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.control.Toggle

interface LocationExtension {

    val locationConsumer: Consumer<Location?>?

    val locationSourcesController: ModuleLocationSourcesController?

    val locationControllerAppStateToggle: Toggle?
}
