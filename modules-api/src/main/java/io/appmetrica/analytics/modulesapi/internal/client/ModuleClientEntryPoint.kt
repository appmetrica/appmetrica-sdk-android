package io.appmetrica.analytics.modulesapi.internal.client

interface ModuleClientEntryPoint<T : Any> {

    val identifier: String

    fun initClientSide(clientContext: ClientContext)

    fun onActivated()
}
