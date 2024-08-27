package io.appmetrica.analytics.modulesapi.internal.service

import io.appmetrica.analytics.modulesapi.internal.service.event.ModuleEventServiceHandlerFactory

abstract class ModuleServiceEntryPoint<T : Any> {

    abstract val identifier: String

    open val remoteConfigExtensionConfiguration: RemoteConfigExtensionConfiguration<T>? = null

    open val moduleEventServiceHandlerFactory: ModuleEventServiceHandlerFactory? = null

    open val locationServiceExtension: LocationServiceExtension? = null

    open val moduleServicesDatabase: ModuleServicesDatabase? = null

    open val clientConfigProvider: ClientConfigProvider? = null

    open fun initServiceSide(serviceContext: ServiceContext, initialConfig: ModuleRemoteConfig<T?>) {}
}
