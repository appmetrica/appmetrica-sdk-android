package io.appmetrica.analytics.modulesapi.internal

import io.appmetrica.analytics.modulesapi.internal.event.ModuleEventHandlerFactory

interface ModuleEntryPoint<T : Any> {

    val identifier: String

    val remoteConfigExtensionConfiguration: RemoteConfigExtensionConfiguration<T>?

    val moduleEventHandlerFactory: ModuleEventHandlerFactory?

    val locationExtension: LocationExtension?

    val moduleServicesDatabase: ModuleServicesDatabase?

    fun initServiceSide(serviceContext: ServiceContext, initialConfig: ModuleRemoteConfig<T?>)
}
