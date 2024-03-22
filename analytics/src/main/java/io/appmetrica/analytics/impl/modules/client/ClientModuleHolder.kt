package io.appmetrica.analytics.impl.modules.client

import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint

interface ClientModuleHolder {

    fun registerModule(moduleClientEntryPoint: ModuleClientEntryPoint<Any>)
}
