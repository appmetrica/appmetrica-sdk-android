package io.appmetrica.analytics.impl.modules

import android.location.Location
import io.appmetrica.analytics.coreapi.internal.backport.Consumer
import io.appmetrica.analytics.coreapi.internal.control.Toggle
import io.appmetrica.analytics.modulesapi.internal.ModuleLocationSourcesController
import io.appmetrica.analytics.modulesapi.internal.ModuleServicesDatabase

internal interface ModuleApi : ModulesRemoteConfigProcessingExtension, ModulesRemoteConfigArgumentsCollector {

    fun collectLocationConsumers(): List<Consumer<Location?>>

    fun chooseLocationSourceController(): ModuleLocationSourcesController?

    fun chooseLocationAppStateControlToggle(): Toggle?

    fun collectModuleServiceDatabases(): List<ModuleServicesDatabase>
}
