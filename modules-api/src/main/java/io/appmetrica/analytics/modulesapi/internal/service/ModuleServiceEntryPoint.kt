package io.appmetrica.analytics.modulesapi.internal.service

import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerFactory

interface ModuleServiceEntryPoint<T : Any> {

    val identifier: String

    val remoteConfigExtensionConfiguration: RemoteConfigExtensionConfiguration<T>?

    val moduleEventServiceHandlerFactory: ModuleEventServiceHandlerFactory?

    val locationServiceExtension: LocationServiceExtension?

    val moduleServicesDatabase: ModuleServicesDatabase?

    fun initServiceSide(serviceContext: ServiceContext, initialConfig: ModuleRemoteConfig<T?>)
}
