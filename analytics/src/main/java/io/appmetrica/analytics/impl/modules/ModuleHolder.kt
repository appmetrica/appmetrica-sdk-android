package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.modulesapi.internal.ModuleEntryPoint

interface ModuleHolder {

    fun registerModule(moduleEntryPoint: ModuleEntryPoint<Any>)
}
