package io.appmetrica.analytics.impl.modules.service

import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint

internal interface ServiceModuleHolder {

    fun registerModule(moduleServiceEntryPoint: ModuleServiceEntryPoint<Any>)
}
