package io.appmetrica.analytics.impl.modules.service

import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint

interface ServiceModuleHolder {

    fun registerModule(moduleServiceEntryPoint: ModuleServiceEntryPoint<Any>)
}
