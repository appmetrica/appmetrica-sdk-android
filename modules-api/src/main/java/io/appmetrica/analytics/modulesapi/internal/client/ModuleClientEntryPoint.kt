package io.appmetrica.analytics.modulesapi.internal.client

abstract class ModuleClientEntryPoint<T : Any> {

    abstract val identifier: String

    open fun initClientSide(clientContext: ClientContext) {}

    open fun onActivated() {}
}
