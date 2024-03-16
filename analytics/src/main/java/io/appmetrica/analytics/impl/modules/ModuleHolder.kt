package io.appmetrica.analytics.impl.modules

import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint

interface ModuleHolder {

    fun registerModule(moduleServiceEntryPoint: ModuleServiceEntryPoint<Any>)
}
