package io.appmetrica.analytics.modulesapi.internal.client

abstract class ModuleClientEntryPoint<T : Any> {

    abstract val identifier: String

    open val serviceConfigExtensionConfiguration: ServiceConfigExtensionConfiguration<T>? = null

    open fun initClientSide(clientContext: ClientContext) {}

    open fun onActivated() {}
}
